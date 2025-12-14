package terminal.commands;

import terminal.Logger;
import terminal.commands.annotations.Command;
import terminal.commands.annotations.CommandHandler;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main command framework for annotation-based command processing.
 * <p>
 * This class is responsible for scanning, registering, and executing commands
 * defined using the {@link Command} and {@link CommandHandler} annotations.
 * It provides a modern, annotation-driven approach to command handling.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * // Create framework
 * CommandFramework framework = new CommandFramework();
 *
 * // Register command classes
 * framework.registerCommand(new TeleportCommand());
 * framework.registerCommand(new GiveCommand());
 *
 * // Execute commands
 * framework.executeCommand("teleport player 100 64 100", "console");
 * }</pre>
 * </p>
 *
 * @author riege
 * @version 1.0
 * @since 1.21.4
 */
public class CommandFramework {

    /**
     * The command registry.
     */
    private final CommandRegistry registry;

    /**
     * Executor service for asynchronous command execution.
     */
    private final ExecutorService asyncExecutor;

    /**
     * Registered command routers.
     */
    private final Map<String, CommandRouter> routers;

    /**
     * Global middlewares applied to all commands.
     */
    private final List<CommandMiddleware> globalMiddlewares;

    /**
     * Default sender for commands without explicit sender.
     */
    private static final String DEFAULT_SENDER = "console";

    /**
     * Router scanner for annotation-based routers.
     */
    private final RouterScanner routerScanner;

    /**
     * Constructs a new CommandFramework.
     */
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
     * Registers a command handler instance.
     * <p>
     * This method scans the provided object for {@link Command} and
     * {@link CommandHandler} annotations and registers all found commands.
     * </p>
     *
     * @param commandHandler the command handler instance
     * @return true if registration was successful
     * @throws IllegalArgumentException if commandHandler is null or lacks annotations
     */
    public boolean registerCommand(final Object commandHandler) {
        if (commandHandler == null) {
            throw new IllegalArgumentException("Command handler cannot be null");
        }

        final Class<?> clazz = commandHandler.getClass();
        final Command commandAnnotation = clazz.getAnnotation(Command.class);

        if (commandAnnotation == null) {
            throw new IllegalArgumentException(
                    "Class " + clazz.getName() + " is not annotated with @Command");
        }

        // Check if enabled
        if (!commandAnnotation.enabled()) {
            Logger.debug("Skipping disabled command: " + commandAnnotation.name());
            return false;
        }

        // Create CommandInfo
        final CommandInfo commandInfo = new CommandInfo(commandAnnotation, commandHandler);

        // Scan for handler methods
        boolean foundHandler = false;
        for (final Method method : clazz.getDeclaredMethods()) {
            final CommandHandler handlerAnnotation = method.getAnnotation(CommandHandler.class);
            if (handlerAnnotation != null) {
                // Validate method signature
                if (!validateHandlerMethod(method)) {
                    Logger.warning("Invalid handler method signature: " + method.getName() +
                            " in " + clazz.getName());
                    continue;
                }

                commandInfo.addHandler(method, handlerAnnotation);
                foundHandler = true;
            }
        }

        if (!foundHandler) {
            throw new IllegalArgumentException(
                    "No @CommandHandler methods found in " + clazz.getName());
        }

        // Scan for subcommands
        final List<String> basePath = new ArrayList<>();
        basePath.add(commandAnnotation.name());
        final Map<String, SubCommandInfo> subCommands =
                routerScanner.scanSubCommands(clazz, commandHandler, basePath);

        // Add subcommands to command info
        for (final Map.Entry<String, SubCommandInfo> entry : subCommands.entrySet()) {
            commandInfo.addSubCommand(entry.getKey(), entry.getValue());
        }

        // Register the command
        registry.register(commandInfo);

        Logger.debug("Registered command: " + commandAnnotation.name() +
                " with " + commandInfo.getHandlers().size() + " handler(s)" +
                " and " + subCommands.size() + " subcommand(s)");

        return true;
    }

    /**
     * Validates that a handler method has the correct signature.
     *
     * @param method the method to validate
     * @return true if valid
     */
    private boolean validateHandlerMethod(final Method method) {
        final Class<?>[] paramTypes = method.getParameterTypes();

        // Must have exactly one parameter of type CommandContext
        if (paramTypes.length != 1) {
            return false;
        }

        return paramTypes[0] == CommandContext.class;
    }

    /**
     * Executes a command from raw input.
     *
     * @param input  the raw command input
     * @param sender the command sender
     * @return true if the command was executed successfully
     */
    public boolean executeCommand(final String input, final String sender) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // Parse command and arguments
        final String[] parts = input.trim().split("\\s+");
        final String commandName = parts[0].toLowerCase();
        final String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        return executeCommand(commandName, args, input, sender);
    }

    /**
     * Executes a command with parsed arguments.
     *
     * @param commandName the command name
     * @param args        the command arguments
     * @param rawInput    the raw input string
     * @param sender      the command sender
     * @return true if the command was executed successfully
     */
    public boolean executeCommand(
            final String commandName,
            final String[] args,
            final String rawInput,
            final String sender
    ) {
        // Look up command
        final var commandOptional = registry.getCommand(commandName);

        if (commandOptional.isEmpty()) {
            Logger.error("Unknown command: " + commandName);
            return false;
        }

        final CommandInfo commandInfo = commandOptional.get();

        // Check if enabled
        if (!commandInfo.isEnabled()) {
            Logger.error("Command '" + commandName + "' is disabled");
            return false;
        }

        // Validate argument count
        final Command cmd = commandInfo.getCommand();
        if (cmd.minArgs() > 0 && args.length < cmd.minArgs()) {
            Logger.error("Not enough arguments! Expected at least " + cmd.minArgs());
            if (!cmd.usage().isEmpty()) {
                Logger.error("Usage: " + cmd.usage());
            }
            return false;
        }

        if (cmd.maxArgs() >= 0 && args.length > cmd.maxArgs()) {
            Logger.error("Too many arguments! Expected at most " + cmd.maxArgs());
            if (!cmd.usage().isEmpty()) {
                Logger.error("Usage: " + cmd.usage());
            }
            return false;
        }

        // Create context
        final CommandContext context = new CommandContext(
                commandName,
                args,
                rawInput,
                sender != null ? sender : DEFAULT_SENDER
        );

        // Store command instance in context for middleware access
        context.setData("commandInstance", commandInfo.getHandlerInstance());

        // Set usage string
        if (!cmd.usage().isEmpty()) {
            context.setUsage(cmd.usage());
        }

        // Check for subcommands first
        if (args.length > 0 && commandInfo.hasSubCommands()) {
            final SubCommandInfo subCommand = commandInfo.findSubCommand(args);
            if (subCommand != null) {
                return executeSubCommand(subCommand, commandInfo, args, rawInput, sender);
            }
        }

        // Find appropriate handler
        final CommandInfo.HandlerInfo handler = commandInfo.findHandler(context);

        if (handler == null) {
            Logger.error("No handler found for command: " + commandName);
            if (commandInfo.hasSubCommands()) {
                Logger.info("Available subcommands: " +
                        String.join(", ", commandInfo.getSubCommands().keySet()));
            }
            return false;
        }

        // Execute through middleware pipeline
        return executeWithMiddleware(commandInfo, handler, context);
    }

    /**
     * Executes a subcommand.
     *
     * @param subCommand the subcommand info
     * @param parentInfo the parent command info
     * @param args       the original arguments
     * @param rawInput   the raw input
     * @param sender     the sender
     * @return {@code true} if successful
     */
    private boolean executeSubCommand(
            final SubCommandInfo subCommand,
            final CommandInfo parentInfo,
            final String[] args,
            final String rawInput,
            final String sender
    ) {
        // Calculate remaining args after subcommand path
        final int pathLength = subCommand.getPath().size() - 1; // -1 for base command
        final String[] subArgs = Arrays.copyOfRange(args, pathLength, args.length);

        // Validate args
        if (subCommand.getAnnotation().minArgs() > 0 &&
                subArgs.length < subCommand.getAnnotation().minArgs()) {
            Logger.error("Not enough arguments for subcommand!");
            if (!subCommand.getAnnotation().usage().isEmpty()) {
                Logger.error("Usage: " + subCommand.getAnnotation().usage());
            }
            return false;
        }

        if (subCommand.getAnnotation().maxArgs() >= 0 &&
                subArgs.length > subCommand.getAnnotation().maxArgs()) {
            Logger.error("Too many arguments for subcommand!");
            if (!subCommand.getAnnotation().usage().isEmpty()) {
                Logger.error("Usage: " + subCommand.getAnnotation().usage());
            }
            return false;
        }

        // Create context for subcommand
        final CommandContext context = new CommandContext(
                subCommand.getCommandString(),
                subArgs,
                rawInput,
                sender != null ? sender : DEFAULT_SENDER
        );

        context.setData("commandInstance", parentInfo.getHandlerInstance());
        context.setData("subCommandInstance", subCommand.getInstance());
        context.setData("subCommandPath", subCommand.getPath());

        if (!subCommand.getAnnotation().usage().isEmpty()) {
            context.setUsage(subCommand.getAnnotation().usage());
        }

        // Execute handler
        if (subCommand.getHandler() == null) {
            Logger.error("No handler found for subcommand: " + subCommand.getCommandString());
            if (subCommand.hasSubCommands()) {
                Logger.info("Available subcommands: " +
                        String.join(", ", subCommand.getSubCommands().keySet()));
            }
            return false;
        }

        // Execute through middleware
        return executeSubCommandWithMiddleware(subCommand, context);
    }

    /**
     * Executes a subcommand through middleware pipeline.
     *
     * @param subCommand the subcommand
     * @param context    the context
     * @return {@code true} if successful
     */
    private boolean executeSubCommandWithMiddleware(
            final SubCommandInfo subCommand,
            final CommandContext context
    ) {
        // Collect middlewares
        final List<CommandMiddleware> allMiddlewares = new ArrayList<>(globalMiddlewares);

        // Create pipeline
        final CommandPipeline pipeline = new CommandPipeline(allMiddlewares);

        // Execute
        return pipeline.execute(context, () -> {
            try {
                subCommand.getHandler().invoke(subCommand.getInstance(), context);
                return true;
            } catch (Exception e) {
                Logger.error("Error executing subcommand: " + context.getCommandName());
                Logger.error("Reason: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                if (e.getCause() != null) {
                    e.getCause().printStackTrace();
                } else {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    /**
     * Executes a command through the middleware pipeline.
     *
     * @param commandInfo the command info
     * @param handler     the handler
     * @param context     the context
     * @return {@code true} if successful
     */
    private boolean executeWithMiddleware(
            final CommandInfo commandInfo,
            final CommandInfo.HandlerInfo handler,
            final CommandContext context
    ) {
        // Collect all middlewares: global + router-specific
        final List<CommandMiddleware> allMiddlewares = new ArrayList<>(globalMiddlewares);

        // Check if command belongs to a router
        final String routerName = context.getData("router", null);
        if (routerName != null && routers.containsKey(routerName)) {
            final CommandRouter router = routers.get(routerName);
            allMiddlewares.addAll(router.getMiddlewares());
        }

        // Create pipeline
        final CommandPipeline pipeline = new CommandPipeline(allMiddlewares);

        // Execute through pipeline
        return pipeline.execute(context, () -> {
            invokeHandler(commandInfo, handler, context);
            return true;
        });
    }

    /**
     * Invokes a command handler.
     *
     * @param commandInfo the command information
     * @param handler     the handler information
     * @param context     the command context
     * @return true if execution was successful
     */
    private boolean invokeHandler(
            final CommandInfo commandInfo,
            final CommandInfo.HandlerInfo handler,
            final CommandContext context
    ) {
        final Runnable execution = () -> {
            try {
                handler.getMethod().invoke(commandInfo.getHandlerInstance(), context);
            } catch (final Exception e) {
                Logger.error("Error executing command '" + context.getCommandName() + "'");
                Logger.error("Reason: " + e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                if (e.getCause() != null) {
                    e.getCause().printStackTrace();
                } else {
                    e.printStackTrace();
                }
            }
        };

        // Execute async or sync based on annotation
        if (handler.isAsync()) {
            asyncExecutor.submit(execution);
        } else {
            execution.run();
        }

        return true;
    }

    /**
     * Executes a command with default sender (console).
     *
     * @param input the raw command input
     * @return true if executed successfully
     */
    public boolean executeCommand(final String input) {
        return executeCommand(input, DEFAULT_SENDER);
    }

    /**
     * Returns the command registry.
     *
     * @return the registry
     */
    public CommandRegistry getRegistry() {
        return registry;
    }

    /**
     * Unregisters a command by name.
     *
     * @param name the command name
     * @return true if unregistered successfully
     */
    public boolean unregisterCommand(final String name) {
        return registry.unregister(name);
    }

    /**
     * Checks if a command is registered.
     *
     * @param name the command name or alias
     * @return true if the command exists
     */
    public boolean hasCommand(final String name) {
        return registry.hasCommand(name);
    }

    /**
     * Registers a command router from an annotated class.
     * <p>
     * Scans the router class for @Router annotation and all nested @Command classes.
     * </p>
     *
     * @param routerClass the router class
     * @return {@code true} if registration was successful
     */
    public boolean registerRouter(final Class<?> routerClass) {
        if (routerClass == null) {
            throw new IllegalArgumentException("Router class cannot be null");
        }

        // Scan router for commands
        final List<Object> commands = routerScanner.scanRouter(routerClass);

        // Register all commands
        int registeredCount = 0;
        for (final Object command : commands) {
            if (registerCommand(command)) {
                registeredCount++;
            }
        }

        final terminal.commands.annotations.Router annotation =
                routerClass.getAnnotation(terminal.commands.annotations.Router.class);

        if (annotation != null) {
            Logger.info("Registered @Router '" + annotation.name() +
                    "' with " + registeredCount + " command(s)");
        }

        return registeredCount > 0;
    }

    /**
     * Registers a command router instance.
     *
     * @param router the router to register
     * @return {@code true} if registration was successful
     */
    public boolean registerRouter(final CommandRouter router) {
        if (router == null) {
            throw new IllegalArgumentException("Router cannot be null");
        }

        routers.put(router.getName(), router);

        // Register all commands from the router
        int registeredCount = 0;
        for (final Object command : router.getCommands().values()) {
            if (registerCommand(command)) {
                registeredCount++;
            }
        }

        Logger.debug("Registered router '" + router.getName() +
                "' with " + registeredCount + " command(s)");

        return registeredCount > 0;
    }

    /**
     * Adds a global middleware that applies to all commands.
     *
     * @param middleware the middleware to add
     * @return this framework for chaining
     */
    public CommandFramework use(final CommandMiddleware middleware) {
        if (middleware == null) {
            throw new IllegalArgumentException("Middleware cannot be null");
        }
        globalMiddlewares.add(middleware);
        Logger.debug("Added global middleware: " + middleware.getClass().getSimpleName());
        return this;
    }

    /**
     * Gets all registered routers.
     *
     * @return unmodifiable map of routers
     */
    public Map<String, CommandRouter> getRouters() {
        return Collections.unmodifiableMap(routers);
    }

    /**
     * Gets a router by name.
     *
     * @param name the router name
     * @return the router or {@code null} if not found
     */
    public CommandRouter getRouter(final String name) {
        return routers.get(name);
    }

    /**
     * Gets all global middlewares.
     *
     * @return unmodifiable list of middlewares
     */
    public List<CommandMiddleware> getGlobalMiddlewares() {
        return Collections.unmodifiableList(globalMiddlewares);
    }

    /**
     * Shuts down the command framework and releases resources.
     */
    public void shutdown() {
        asyncExecutor.shutdown();
        registry.clear();
        routers.clear();
        globalMiddlewares.clear();
        Logger.info("Command framework shut down");
    }

    /**
     * Returns the number of registered commands.
     *
     * @return the command count
     */
    public int getCommandCount() {
        return registry.size();
    }

    @Override
    public String toString() {
        return "CommandFramework{" +
                "commands=" + registry.size() +
                ", routers=" + routers.size() +
                ", globalMiddlewares=" + globalMiddlewares.size() +
                '}';
    }
}
