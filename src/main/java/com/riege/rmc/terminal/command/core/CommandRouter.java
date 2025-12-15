package com.riege.rmc.terminal.command.core;

import com.riege.rmc.terminal.command.annotations.Command;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Router system for organizing commands into logical groups/modules.
 * <p>
 * Routers allow grouping related commands together and applying
 * middleware at the router level for all commands in that group.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * CommandRouter adminRouter = new CommandRouter("admin");
 * adminRouter.register(new BanCommand());
 * adminRouter.register(new KickCommand());
 * adminRouter.middleware(new AdminPermissionMiddleware());
 * </pre>
 *
 * @author riege
 * @version 1.0
 */
public class CommandRouter {

    private final String name;
    private final String prefix;
    private final Map<String, Object> commands;
    private final List<CommandMiddleware> middlewares;
    private final Map<String, String> metadata;

    /**
     * Creates a new command router.
     *
     * @param name the router name (used for grouping and logging)
     */
    public CommandRouter(final String name) {
        this(name, "");
    }

    /**
     * Creates a new command router with a prefix.
     * <p>
     * Commands registered to this router will be prefixed automatically.
     * For example, with prefix "admin", a command "ban" becomes "admin:ban".
     * </p>
     *
     * @param name   the router name
     * @param prefix the command prefix
     */
    public CommandRouter(final String name, final String prefix) {
        this.name = name;
        this.prefix = prefix;
        this.commands = new ConcurrentHashMap<>();
        this.middlewares = new ArrayList<>();
        this.metadata = new ConcurrentHashMap<>();
    }

    /**
     * Registers a command to this router.
     *
     * @param command the command instance
     * @return this router for chaining
     */
    public CommandRouter register(final Object command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        final String commandName = extractCommandName(command);
        commands.put(commandName, command);
        return this;
    }

    /**
     * Adds middleware to this router.
     * <p>
     * All commands in this router will execute through this middleware.
     * Middlewares are executed in the order they are added.
     * </p>
     *
     * @param middleware the middleware to add
     * @return this router for chaining
     */
    public CommandRouter middleware(final CommandMiddleware middleware) {
        if (middleware == null) {
            throw new IllegalArgumentException("Middleware cannot be null");
        }
        middlewares.add(middleware);
        return this;
    }

    /**
     * Sets metadata for this router.
     *
     * @param key   the metadata key
     * @param value the metadata value
     * @return this router for chaining
     */
    public CommandRouter meta(final String key, final String value) {
        metadata.put(key, value);
        return this;
    }

    /**
     * Executes a command through this router's middleware pipeline.
     *
     * @param context     the command context
     * @param executor    the actual command executor
     * @return {@code true} if the command was executed successfully
     */
    public boolean executeCommand(final CommandContext context,
                                  final CommandExecutor executor) {
        final CommandPipeline pipeline = new CommandPipeline(middlewares);

        return pipeline.execute(context, () -> {
            executor.execute(context);
            return true;
        });
    }

    /**
     * Gets the router name.
     *
     * @return the router name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the command prefix.
     *
     * @return the command prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets all registered commands.
     *
     * @return unmodifiable map of commands
     */
    public Map<String, Object> getCommands() {
        return Collections.unmodifiableMap(commands);
    }

    /**
     * Gets all middlewares.
     *
     * @return unmodifiable list of middlewares
     */
    public List<CommandMiddleware> getMiddlewares() {
        return Collections.unmodifiableList(middlewares);
    }

    /**
     * Gets router metadata.
     *
     * @return unmodifiable map of metadata
     */
    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Checks if this router has a specific command.
     *
     * @param commandName the command name
     * @return {@code true} if the command exists
     */
    public boolean hasCommand(final String commandName) {
        return commands.containsKey(commandName);
    }

    /**
     * Gets a command by name.
     *
     * @param commandName the command name
     * @return the command instance or {@code null} if not found
     */
    public Object getCommand(final String commandName) {
        return commands.get(commandName);
    }

    /**
     * Extracts the command name from a command instance using reflection.
     *
     * @param command the command instance
     * @return the command name
     */
    private String extractCommandName(final Object command) {
        final Class<?> clazz = command.getClass();
        final Command annotation =
                clazz.getAnnotation(Command.class);

        if (annotation != null) {
            return annotation.name();
        }

        return clazz.getSimpleName().toLowerCase().replace("command", "");
    }

    @Override
    public String toString() {
        return "CommandRouter{" +
                "name='" + name + '\'' +
                ", prefix='" + prefix + '\'' +
                ", commands=" + commands.size() +
                ", middlewares=" + middlewares.size() +
                '}';
    }

    /**
     * Functional interface for command execution.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        void execute(CommandContext context);
    }
}
