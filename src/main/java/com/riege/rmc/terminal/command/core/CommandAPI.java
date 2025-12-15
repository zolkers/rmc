package com.riege.rmc.terminal.command.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Public API for programmatic interaction with the command framework.
 * <p>
 * This class provides a singleton interface for registering, executing,
 * and managing commands throughout the application. It wraps the
 * {@link CommandFramework} to provide a convenient global access point.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * // Get API instance
 * CommandAPI api = CommandAPI.getInstance();
 *
 * // Register commands
 * api.registerCommand(new MyCommand());
 *
 * // Execute commands
 * api.executeCommand("mycommand arg1 arg2");
 *
 * // Query registered commands
 * Collection<String> commands = api.getCommandNames();
 * }</pre>
 * </p>
 *
 * @author riege
 * @version 1.0
 */
public class CommandAPI {

    /**
     * Singleton instance.
     */
    private static volatile CommandAPI instance;

    /**
     * The underlying command framework.
     */
    private CommandFramework framework;

    /**
     * Flag indicating if the API is initialized.
     */
    private volatile boolean initialized;

    /**
     * Private constructor for singleton pattern.
     */
    private CommandAPI() {
        this.initialized = false;
    }

    /**
     * Returns the singleton instance of CommandAPI.
     *
     * @return the CommandAPI instance
     */
    public static CommandAPI getInstance() {
        if (instance == null) {
            synchronized (CommandAPI.class) {
                if (instance == null) {
                    instance = new CommandAPI();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the API with a command framework instance.
     * <p>
     * This method should be called once during application startup.
     * </p>
     *
     * @param framework the command framework
     * @return true if initialization was successful
     * @throws IllegalArgumentException if framework is null
     */
    public synchronized boolean initialize(final CommandFramework framework) {
        if (framework == null) {
            throw new IllegalArgumentException("CommandFramework cannot be null");
        }
        if (!initialized) {
            this.framework = framework;
            this.initialized = true;
            return true;
        }
        return false;
    }

    /**
     * Initializes the API with a new command framework.
     *
     * @return true if initialization was successful
     */
    public synchronized boolean initialize() {
        return initialize(new CommandFramework());
    }

    /**
     * Checks if the API is initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Ensures the API is initialized.
     *
     * @throws IllegalStateException if not initialized
     */
    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException(
                    "CommandAPI not initialized. Call initialize() first.");
        }
    }

    // ========== Command Registration Methods ==========

    /**
     * Registers a command handler instance.
     *
     * @param commandHandler the command handler
     * @return true if registration was successful
     * @throws IllegalStateException if API is not initialized
     */
    public boolean registerCommand(final Object commandHandler) {
        ensureInitialized();
        return framework.registerCommand(commandHandler);
    }

    /**
     * Registers multiple command handlers.
     *
     * @param commandHandlers the command handlers
     * @return number of successfully registered commands
     * @throws IllegalStateException if API is not initialized
     */
    public int registerCommands(final Object... commandHandlers) {
        ensureInitialized();
        int count = 0;
        for (final Object handler : commandHandlers) {
            if (framework.registerCommand(handler)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Unregisters a command by name.
     *
     * @param name the command name
     * @return true if unregistered successfully
     * @throws IllegalStateException if API is not initialized
     */
    public boolean unregisterCommand(final String name) {
        ensureInitialized();
        return framework.unregisterCommand(name);
    }

    // ========== Command Execution Methods ==========

    /**
     * Executes a command from raw input.
     *
     * @param input  the command input
     * @param sender the command sender
     * @return true if execution was successful
     * @throws IllegalStateException if API is not initialized
     */
    public boolean executeCommand(final String input, final String sender) {
        ensureInitialized();
        return framework.executeCommand(input, sender);
    }

    /**
     * Executes a command with default sender (console).
     *
     * @param input the command input
     * @return true if execution was successful
     * @throws IllegalStateException if API is not initialized
     */
    public boolean executeCommand(final String input) {
        ensureInitialized();
        return framework.executeCommand(input);
    }

    /**
     * Executes a command asynchronously.
     *
     * @param input  the command input
     * @param sender the command sender
     * @return CompletableFuture that completes when execution finishes
     * @throws IllegalStateException if API is not initialized
     */
    public CompletableFuture<Boolean> executeCommandAsync(
            final String input,
            final String sender
    ) {
        return CompletableFuture.supplyAsync(() -> executeCommand(input, sender));
    }

    /**
     * Executes a command asynchronously with default sender.
     *
     * @param input the command input
     * @return CompletableFuture that completes when execution finishes
     * @throws IllegalStateException if API is not initialized
     */
    public CompletableFuture<Boolean> executeCommandAsync(final String input) {
        return CompletableFuture.supplyAsync(() -> executeCommand(input));
    }

    // ========== Query Methods ==========

    /**
     * Checks if a command is registered.
     *
     * @param name the command name or alias
     * @return true if the command exists
     * @throws IllegalStateException if API is not initialized
     */
    public boolean hasCommand(final String name) {
        ensureInitialized();
        return framework.hasCommand(name);
    }

    /**
     * Retrieves command information by name.
     *
     * @param name the command name or alias
     * @return Optional containing the CommandInfo, or empty if not found
     * @throws IllegalStateException if API is not initialized
     */
    public Optional<CommandInfo> getCommand(final String name) {
        ensureInitialized();
        return framework.getRegistry().getCommand(name);
    }

    /**
     * Returns all registered command names.
     *
     * @return collection of command names
     * @throws IllegalStateException if API is not initialized
     */
    public Collection<String> getCommandNames() {
        ensureInitialized();
        return framework.getRegistry().getCommandNames();
    }

    /**
     * Returns all registered commands.
     *
     * @return collection of CommandInfo objects
     * @throws IllegalStateException if API is not initialized
     */
    public Collection<CommandInfo> getAllCommands() {
        ensureInitialized();
        return framework.getRegistry().getAllCommands();
    }

    /**
     * Returns all registered aliases.
     *
     * @return map of aliases to command names
     * @throws IllegalStateException if API is not initialized
     */
    public Map<String, String> getAliases() {
        ensureInitialized();
        return framework.getRegistry().getAliases();
    }

    /**
     * Searches for commands matching a partial name.
     *
     * @param partial the partial command name
     * @return list of matching command names
     * @throws IllegalStateException if API is not initialized
     */
    public List<String> findMatchingCommands(final String partial) {
        ensureInitialized();
        return framework.getRegistry().findMatchingCommands(partial);
    }

    /**
     * Returns the number of registered commands.
     *
     * @return the command count
     * @throws IllegalStateException if API is not initialized
     */
    public int getCommandCount() {
        ensureInitialized();
        return framework.getCommandCount();
    }

    /**
     * Returns registry statistics.
     *
     * @return map containing statistics
     * @throws IllegalStateException if API is not initialized
     */
    public Map<String, Object> getStatistics() {
        ensureInitialized();
        return framework.getRegistry().getStatistics();
    }

    // ========== Advanced Methods ==========

    /**
     * Returns the underlying command framework.
     * <p>
     * Use with caution as this exposes internal implementation.
     * </p>
     *
     * @return the command framework
     * @throws IllegalStateException if API is not initialized
     */
    public CommandFramework getFramework() {
        ensureInitialized();
        return framework;
    }

    /**
     * Returns the command registry.
     *
     * @return the registry
     * @throws IllegalStateException if API is not initialized
     */
    public CommandRegistry getRegistry() {
        ensureInitialized();
        return framework.getRegistry();
    }

    // ========== Lifecycle Methods ==========

    /**
     * Shuts down the API and releases resources.
     */
    public synchronized void shutdown() {
        if (initialized) {
            framework.shutdown();
            framework = null;
            initialized = false;
        }
    }

    /**
     * Resets the API to its initial state.
     * <p>
     * This method is primarily for testing purposes.
     * </p>
     */
    public static synchronized void reset() {
        if (instance != null) {
            instance.shutdown();
            instance = null;
        }
    }

    @Override
    public String toString() {
        if (!initialized) {
            return "CommandAPI{not initialized}";
        }
        return "CommandAPI{" +
                "commands=" + framework.getCommandCount() +
                '}';
    }
}
