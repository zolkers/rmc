package com.riege.rmc.terminal.command.core;

import com.riege.rmc.terminal.logging.Logger;
import com.riege.rmc.terminal.command.annotations.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * main.java.com.riege.rmc.Main command framework for annotation-based command processing.
 * <p>
 * This class is responsible for scanning, registering, and executing commands.
 * It handles parameter injection, type conversion, and middleware execution.
 * </p>
 *
 * @author riege
 * @version 1.1
 */
public final class CommandFramework {

    private final CommandRegistry registry;
    private final ExecutorService asyncExecutor;
    private final Map<String, CommandRouter> routers;
    private final List<CommandMiddleware> globalMiddlewares;
    private final RouterScanner routerScanner;

    // Default permission provider (allows everything by default)
    private PermissionProvider permissionProvider = (sender, node) -> true;

    private static final String DEFAULT_SENDER = "console";

    public CommandFramework() {
        this.registry = new CommandRegistry();
        this.asyncExecutor = Executors.newCachedThreadPool(r -> {
            final Thread thread = new Thread(r, "CommandFramework-Async");
            thread.setDaemon(true);
            return thread;
        });
        this.routers = new ConcurrentHashMap<>();
        this.globalMiddlewares = new ArrayList<>();
        this.routerScanner = new RouterScanner();
    }

    /**
     * Sets the permission provider for the framework.
     * @param provider the provider implementation
     */
    public void setPermissionProvider(PermissionProvider provider) {
        this.permissionProvider = provider;
        // Update permission middleware if it exists, or user can add it manually
    }

    public PermissionProvider getPermissionProvider() {
        return permissionProvider;
    }

    // ================= Registration Logic =================

    public boolean registerCommand(final Object commandHandler) {
        if (commandHandler == null) throw new IllegalArgumentException("Handler cannot be null");

        final Class<?> clazz = commandHandler.getClass();
        final Command commandAnnotation = clazz.getAnnotation(Command.class);

        if (commandAnnotation == null) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @Command");
        }

        if (!commandAnnotation.enabled()) return false;

        final CommandInfo commandInfo = new CommandInfo(commandAnnotation, commandHandler);
        boolean foundHandler = false;

        // Use getMethods() to include inherited methods from superclasses
        for (final Method method : clazz.getMethods()) {
            final CommandHandler handlerAnnotation = method.getAnnotation(CommandHandler.class);
            if (handlerAnnotation != null) {
                // We no longer strictly enforce just (CommandContext ctx)
                // We allow parameter injection now.
                commandInfo.addHandler(method, handlerAnnotation);
                foundHandler = true;
            }
        }

        if (!foundHandler) {
            throw new IllegalArgumentException("No @CommandHandler methods found in " + clazz.getName());
        }

        final List<String> basePath = new ArrayList<>();
        basePath.add(commandAnnotation.name());
        final Map<String, SubCommandInfo> subCommands = routerScanner.scanSubCommands(clazz, commandHandler, basePath);

        for (final Map.Entry<String, SubCommandInfo> entry : subCommands.entrySet()) {
            commandInfo.addSubCommand(entry.getKey(), entry.getValue());
        }

        registry.register(commandInfo);
        Logger.debug("Registered command: " + commandAnnotation.name());
        return true;
    }

    // ================= Execution Logic =================

    public boolean executeCommand(final String input, final String sender) {
        if (input == null || input.trim().isEmpty()) return false;

        final String[] parts = input.trim().split("\\s+");
        final String commandName = parts[0].toLowerCase();
        final String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        return executeCommand(commandName, args, input, sender);
    }

    public boolean executeCommand(final String commandName, final String[] args, final String rawInput, final String sender) {
        final var commandOptional = registry.getCommand(commandName);

        if (commandOptional.isEmpty()) {
            Logger.error("Unknown command: " + commandName);
            return false;
        }

        final CommandInfo commandInfo = commandOptional.get();
        if (!commandInfo.isEnabled()) {
            Logger.error("Command '" + commandName + "' is disabled");
            return false;
        }

        // Basic argument count check based on @Command annotation
        final Command cmd = commandInfo.getCommand();
        if (cmd.minArgs() > 0 && args.length < cmd.minArgs()) {
            Logger.error("Not enough arguments! Expected at least " + cmd.minArgs());
            if (!cmd.usage().isEmpty()) Logger.error("Usage: " + cmd.usage());
            return false;
        }

        final CommandContext context = new CommandContext(
                commandName, args, rawInput, sender != null ? sender : DEFAULT_SENDER
        );
        context.setData("commandInstance", commandInfo.getHandlerInstance());
        if (!cmd.usage().isEmpty()) context.setUsage(cmd.usage());

        // Subcommand handling
        if (args.length > 0 && commandInfo.hasSubCommands()) {
            final SubCommandInfo subCommand = commandInfo.findSubCommand(args);
            if (subCommand != null) {
                return executeSubCommand(subCommand, commandInfo, args, rawInput, sender);
            }
        }

        final CommandInfo.HandlerInfo handler = commandInfo.findHandler(context);
        if (handler == null) {
            Logger.error("No handler found for command: " + commandName);
            return false;
        }

        return executeWithMiddleware(commandInfo, handler, context);
    }

    private boolean executeSubCommand(final SubCommandInfo subCommand, final CommandInfo parentInfo, final String[] args, final String rawInput, final String sender) {
        final int pathLength = subCommand.getPath().size() - 1;
        final String[] subArgs = Arrays.copyOfRange(args, pathLength, args.length);

        final CommandContext context = new CommandContext(
                subCommand.getCommandString(), subArgs, rawInput, sender != null ? sender : DEFAULT_SENDER
        );

        context.setData("commandInstance", parentInfo.getHandlerInstance());
        context.setData("subCommandInstance", subCommand.getInstance());

        if (subCommand.getHandler() == null) return false;

        // Execute logic (simplified for subcommands, usually reuse middleware logic)
        return executeSubCommandWithMiddleware(subCommand, context);
    }

    // ================= Middleware Pipeline =================

    private boolean executeWithMiddleware(final CommandInfo commandInfo, final CommandInfo.HandlerInfo handler, final CommandContext context) {
        final List<CommandMiddleware> allMiddlewares = new ArrayList<>(globalMiddlewares);

        final String routerName = context.getData("router", null);
        if (routerName != null && routers.containsKey(routerName)) {
            allMiddlewares.addAll(routers.get(routerName).getMiddlewares());
        }

        final CommandPipeline pipeline = new CommandPipeline(allMiddlewares);
        return pipeline.execute(context, () -> invokeHandler(commandInfo.getHandlerInstance(), handler.getMethod(), handler.isAsync(), context));
    }

    private boolean executeSubCommandWithMiddleware(final SubCommandInfo subCommand, final CommandContext context) {
        final List<CommandMiddleware> allMiddlewares = new ArrayList<>(globalMiddlewares);
        final CommandPipeline pipeline = new CommandPipeline(allMiddlewares);

        // Note: Subcommands usually don't support async annotation directly in this impl, default to sync
        return pipeline.execute(context, () -> invokeHandler(subCommand.getInstance(), subCommand.getHandler(), false, context));
    }

    // ================= Parameter Injection & Invocation =================

    /**
     * Invokes the method with automatic parameter resolution.
     */
    private boolean invokeHandler(final Object instance, final Method method, final boolean async, final CommandContext context) {
        final Runnable execution = () -> {
            try {
                // CORE LOGIC: Resolve parameters based on annotations
                final Object[] params = resolveParameters(method, context);
                method.invoke(instance, params);
            } catch (Exception e) {
                Logger.error("Error executing command '" + context.getCommandName() + "'");
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                Logger.error("Reason: " + cause.getMessage());
                // cause.printStackTrace(); // Uncomment for debug
            }
        };

        if (async) {
            asyncExecutor.submit(execution);
        } else {
            execution.run();
        }
        return true;
    }

    /**
     * Maps CommandContext data to method parameters using reflection and annotations.
     */
    private Object[] resolveParameters(Method method, CommandContext ctx) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        // Index for positional arguments (excluding flags/options/context)
        int positionalArgIndex = 0;

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> type = param.getType();

            if (type.equals(CommandContext.class)) {
                args[i] = ctx;
                continue;
            }

            Flag flagAnn = param.getAnnotation(Flag.class);
            if (flagAnn != null) {
                if (flagAnn.repeatable()) {
                    int count = ctx.getFlagCount(flagAnn.name());
                    if (count == 0 && !flagAnn.shortName().isEmpty()) {
                        count = ctx.getFlagCount(flagAnn.shortName());
                    }
                    args[i] = count;
                } else {
                    boolean hasFlag = ctx.hasFlag(flagAnn.name());
                    if (!hasFlag && !flagAnn.shortName().isEmpty()) {
                        hasFlag = ctx.hasFlag(flagAnn.shortName());
                    }
                    args[i] = hasFlag;
                }
                continue;
            }

            Option optAnn = param.getAnnotation(Option.class);
            if (optAnn != null) {
                String value = ctx.getOption(optAnn.name());
                if (value == null && !optAnn.shortName().isEmpty()) {
                    value = ctx.getOption(optAnn.shortName());
                }

                if (value == null) {
                    if (optAnn.required()) {
                        throw new IllegalArgumentException("Missing required option: --" + optAnn.name());
                    }
                    value = optAnn.defaultValue();
                    if (value.isEmpty()) value = null;
                }

                args[i] = convertType(value, type);
                continue;
            }

            Argument argAnn = param.getAnnotation(Argument.class);

            if (positionalArgIndex >= ctx.getArgs().length) {
                if (argAnn != null && !argAnn.defaultValue().isEmpty()) {
                    args[i] = convertType(argAnn.defaultValue(), type);
                } else if (argAnn != null && !argAnn.required()) {
                    args[i] = getDefaultValue(type);
                } else {

                    args[i] = getDefaultValue(type);
                }
            } else {
                String rawValue = ctx.getArg(positionalArgIndex);

                // Regex validation
                if (argAnn != null && !argAnn.pattern().isEmpty()) {
                    if (!rawValue.matches(argAnn.pattern())) {
                        throw new IllegalArgumentException("Argument '" + rawValue + "' format invalid.");
                    }
                }

                args[i] = convertType(rawValue, type);
            }
            positionalArgIndex++;
        }

        return args;
    }

    private Object convertType(String value, Class<?> targetType) {
        if (value == null) return getDefaultValue(targetType);
        if (targetType == String.class) return value;

        try {
            if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
            if (targetType == long.class || targetType == Long.class) return Long.parseLong(value);
            if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value);
            if (targetType == float.class || targetType == Float.class) return Float.parseFloat(value);
            if (targetType == boolean.class || targetType == Boolean.class) {
                return value.equalsIgnoreCase("true") || value.equals("1") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("on");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert '" + value + "' to " + targetType.getSimpleName());
        }

        return value; // Fallback
    }

    private Object getDefaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == long.class) return 0L;
        return null;
    }

    // ================= Utils & Getters =================

    public boolean executeCommand(final String input) {
        return executeCommand(input, DEFAULT_SENDER);
    }

    public CommandRegistry getRegistry() { return registry; }

    public boolean unregisterCommand(final String name) { return registry.unregister(name); }

    public boolean hasCommand(final String name) { return registry.hasCommand(name); }

    public boolean registerRouter(final Class<?> routerClass) {
        List<Object> commands = routerScanner.scanRouter(routerClass);
        int count = 0;
        for (Object cmd : commands) {
            if (registerCommand(cmd)) count++;
        }
        return count > 0;
    }

    public boolean registerRouter(final CommandRouter router) {
        routers.put(router.getName(), router);
        int count = 0;
        for (Object cmd : router.getCommands().values()) {
            if (registerCommand(cmd)) count++;
        }
        return count > 0;
    }

    public CommandFramework use(final CommandMiddleware middleware) {
        globalMiddlewares.add(middleware);
        return this;
    }

    public void shutdown() {
        asyncExecutor.shutdown();
        registry.clear();
        routers.clear();
        globalMiddlewares.clear();
        Logger.info("Command framework shut down");
    }

    public int getCommandCount() { return registry.size(); }
}