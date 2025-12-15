package com.riege.rmc.terminal.command.core;

import com.riege.rmc.terminal.command.annotations.Command;
import com.riege.rmc.terminal.command.annotations.CommandHandler;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Stores metadata about a registered command.
 * <p>
 * This class encapsulates all information about a command including its
 * annotation metadata, handler methods, and the instance to invoke handlers on.
 * </p>
 *
 * @author riege
 * @version 1.0
 */
public class CommandInfo {

    /**
     * The command annotation metadata.
     */
    private final Command command;

    /**
     * The command handler instance.
     */
    private final Object handlerInstance;

    /**
     * List of handler methods and their metadata.
     */
    private final List<HandlerInfo> handlers;

    /**
     * Map of subcommands by their full path.
     */
    private final Map<String, SubCommandInfo> subCommands;

    /**
     * Constructs a CommandInfo with the specified parameters.
     *
     * @param command         the command annotation
     * @param handlerInstance the handler instance
     */
    public CommandInfo(final Command command, final Object handlerInstance) {
        this.command = Objects.requireNonNull(command, "Command annotation cannot be null");
        this.handlerInstance = Objects.requireNonNull(handlerInstance, "Handler instance cannot be null");
        this.handlers = new ArrayList<>();
        this.subCommands = new HashMap<>();
    }

    /**
     * Adds a handler method to this command.
     *
     * @param method          the handler method
     * @param handlerAnnotation the CommandHandler annotation
     */
    public void addHandler(final Method method, final CommandHandler handlerAnnotation) {
        handlers.add(new HandlerInfo(method, handlerAnnotation));
        // Sort by priority (highest first)
        handlers.sort((h1, h2) -> Integer.compare(h2.annotation.priority(), h1.annotation.priority()));
    }

    /**
     * Returns the command annotation.
     *
     * @return the command annotation
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Returns the handler instance.
     *
     * @return the handler instance
     */
    public Object getHandlerInstance() {
        return handlerInstance;
    }

    /**
     * Returns all handler methods.
     *
     * @return unmodifiable list of handler information
     */
    public List<HandlerInfo> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }

    /**
     * Finds the appropriate handler for the given context.
     * <p>
     * If the first argument matches a subcommand handler, that handler is selected.
     * Otherwise, the default handler (with no subcommand) is selected.
     * </p>
     *
     * @param context the command context
     * @return the handler information, or null if no suitable handler found
     */
    public HandlerInfo findHandler(final CommandContext context) {
        if (handlers.isEmpty()) {
            return null;
        }

        // Check for subcommand handlers first
        if (context.getArgCount() > 0) {
            final String firstArg = context.getArg(0);
            for (final HandlerInfo handler : handlers) {
                if (handler.annotation.subcommand().equals(firstArg)) {
                    return handler;
                }
            }
        }

        // Return default handler (no subcommand)
        for (final HandlerInfo handler : handlers) {
            if (handler.annotation.subcommand().isEmpty()) {
                return handler;
            }
        }

        // No suitable handler found
        return null;
    }

    /**
     * Returns the command name.
     *
     * @return the command name
     */
    public String getName() {
        return command.name();
    }

    /**
     * Returns the command description.
     *
     * @return the command description
     */
    public String getDescription() {
        return command.description();
    }

    /**
     * Returns the command aliases.
     *
     * @return array of aliases
     */
    public String[] getAliases() {
        return command.aliases();
    }

    /**
     * Returns the command permission.
     *
     * @return the permission node
     */
    public String getPermission() {
        return command.permission();
    }

    /**
     * Returns the command usage.
     *
     * @return the usage string
     */
    public String getUsage() {
        return command.usage();
    }

    /**
     * Returns the command priority.
     *
     * @return the priority value
     */
    public int getPriority() {
        return command.priority();
    }

    /**
     * Checks if this command is enabled.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return command.enabled();
    }

    /**
     * Adds a subcommand to this command.
     *
     * @param path the full subcommand path
     * @param info the subcommand info
     */
    public void addSubCommand(final String path, final SubCommandInfo info) {
        subCommands.put(path, info);
    }

    /**
     * Finds a subcommand by path.
     *
     * @param args the command arguments
     * @return the subcommand info or {@code null} if not found
     */
    public SubCommandInfo findSubCommand(final String[] args) {
        if (args.length == 0 || subCommands.isEmpty()) {
            return null;
        }

        // Try to match longest path first
        for (int i = args.length; i > 0; i--) {
            final String[] pathArgs = Arrays.copyOfRange(args, 0, i);
            final String path = String.join(" ", pathArgs);
            final SubCommandInfo info = subCommands.get(path);
            if (info != null) {
                return info;
            }
        }

        return null;
    }

    /**
     * Gets all subcommands.
     *
     * @return unmodifiable map of subcommands
     */
    public Map<String, SubCommandInfo> getSubCommands() {
        return Collections.unmodifiableMap(subCommands);
    }

    /**
     * Checks if this command has subcommands.
     *
     * @return {@code true} if has subcommands
     */
    public boolean hasSubCommands() {
        return !subCommands.isEmpty();
    }

    @Override
    public String toString() {
        return "CommandInfo{" +
                "name='" + command.name() + '\'' +
                ", handlers=" + handlers.size() +
                ", subCommands=" + subCommands.size() +
                '}';
    }

    /**
     * Stores information about a command handler method.
     */
    public static class HandlerInfo {
        /**
         * The handler method.
         */
        private final Method method;

        /**
         * The CommandHandler annotation.
         */
        private final CommandHandler annotation;

        /**
         * Constructs a HandlerInfo.
         *
         * @param method     the handler method
         * @param annotation the CommandHandler annotation
         */
        public HandlerInfo(final Method method, final CommandHandler annotation) {
            this.method = Objects.requireNonNull(method, "Handler method cannot be null");
            this.annotation = Objects.requireNonNull(annotation, "CommandHandler annotation cannot be null");
            method.setAccessible(true); // Allow private methods
        }

        /**
         * Returns the handler method.
         *
         * @return the method
         */
        public Method getMethod() {
            return method;
        }

        /**
         * Returns the CommandHandler annotation.
         *
         * @return the annotation
         */
        public CommandHandler getAnnotation() {
            return annotation;
        }

        /**
         * Returns the subcommand name.
         *
         * @return the subcommand, or empty string for default handler
         */
        public String getSubcommand() {
            return annotation.subcommand();
        }

        /**
         * Checks if this handler is asynchronous.
         *
         * @return true if async
         */
        public boolean isAsync() {
            return annotation.async();
        }

        @Override
        public String toString() {
            return "HandlerInfo{" +
                    "method=" + method.getName() +
                    ", subcommand='" + annotation.subcommand() + '\'' +
                    ", async=" + annotation.async() +
                    '}';
        }
    }
}
