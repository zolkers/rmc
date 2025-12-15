package com.riege.rmc.terminal.command.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing registered commands.
 * <p>
 * This class maintains a thread-safe mapping of command names and aliases
 * to their corresponding {@link CommandInfo} objects. It provides efficient
 * lookup and management capabilities for the command framework.
 * </p>
 *
 * @author riege
 * @version 1.0
 */
public final class CommandRegistry {

    /**
     * Map of command names to CommandInfo objects.
     */
    private final Map<String, CommandInfo> commands;

    /**
     * Map of aliases to command names.
     */
    private final Map<String, String> aliases;

    /**
     * Constructs a new CommandRegistry.
     */
    public CommandRegistry() {
        this.commands = new ConcurrentHashMap<>();
        this.aliases = new ConcurrentHashMap<>();
    }

    /**
     * Registers a command.
     * <p>
     * This method stores the command by its primary name and creates
     * mappings for all aliases. If a command or alias with the same name
     * already exists, it will be replaced.
     * </p>
     *
     * @param commandInfo the command information to register
     * @throws IllegalArgumentException if commandInfo is null
     */
    public void register(final CommandInfo commandInfo) {
        if (commandInfo == null) {
            throw new IllegalArgumentException("CommandInfo cannot be null");
        }

        final String name = commandInfo.getName().toLowerCase();

        // Register the main command
        commands.put(name, commandInfo);

        // Register all aliases
        for (final String alias : commandInfo.getAliases()) {
            final String lowerAlias = alias.toLowerCase();
            aliases.put(lowerAlias, name);
        }
    }

    /**
     * Unregisters a command by name.
     * <p>
     * This removes the command and all its aliases from the registry.
     * </p>
     *
     * @param name the command name to unregister
     * @return true if the command was removed; false if not found
     */
    public boolean unregister(final String name) {
        if (name == null) {
            return false;
        }

        final String lowerName = name.toLowerCase();
        final CommandInfo removed = commands.remove(lowerName);

        if (removed != null) {
            // Remove all aliases pointing to this command
            aliases.entrySet().removeIf(entry -> entry.getValue().equals(lowerName));
            return true;
        }

        return false;
    }

    /**
     * Retrieves a command by name or alias.
     *
     * @param name the command name or alias
     * @return Optional containing the CommandInfo, or empty if not found
     */
    public Optional<CommandInfo> getCommand(final String name) {
        if (name == null) {
            return Optional.empty();
        }

        final String lowerName = name.toLowerCase();

        // Check direct command name first
        CommandInfo commandInfo = commands.get(lowerName);
        if (commandInfo != null) {
            return Optional.of(commandInfo);
        }

        // Check aliases
        final String aliasTarget = aliases.get(lowerName);
        if (aliasTarget != null) {
            commandInfo = commands.get(aliasTarget);
            return Optional.ofNullable(commandInfo);
        }

        return Optional.empty();
    }

    /**
     * Checks if a command exists.
     *
     * @param name the command name or alias
     * @return true if the command exists
     */
    public boolean hasCommand(final String name) {
        return getCommand(name).isPresent();
    }

    /**
     * Returns all registered commands.
     *
     * @return unmodifiable collection of CommandInfo objects
     */
    public Collection<CommandInfo> getAllCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }

    /**
     * Returns all registered command names.
     *
     * @return unmodifiable collection of command names
     */
    public Collection<String> getCommandNames() {
        return Collections.unmodifiableSet(commands.keySet());
    }

    /**
     * Returns all registered aliases.
     *
     * @return unmodifiable map of aliases to command names
     */
    public Map<String, String> getAliases() {
        return Collections.unmodifiableMap(aliases);
    }

    /**
     * Returns commands sorted by priority.
     *
     * @return list of commands sorted by priority (highest first)
     */
    public List<CommandInfo> getCommandsByPriority() {
        final List<CommandInfo> sorted = new ArrayList<>(commands.values());
        sorted.sort((c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority()));
        return sorted;
    }

    /**
     * Searches for commands matching a partial name.
     * <p>
     * This is useful for tab completion and command suggestions.
     * </p>
     *
     * @param partial the partial command name
     * @return list of matching command names
     */
    public List<String> findMatchingCommands(final String partial) {
        if (partial == null || partial.isEmpty()) {
            return new ArrayList<>(commands.keySet());
        }

        final String lowerPartial = partial.toLowerCase();
        final List<String> matches = new ArrayList<>();

        // Check command names
        for (final String name : commands.keySet()) {
            if (name.startsWith(lowerPartial)) {
                matches.add(name);
            }
        }

        // Check aliases
        for (final Map.Entry<String, String> entry : aliases.entrySet()) {
            if (entry.getKey().startsWith(lowerPartial)) {
                matches.add(entry.getKey());
            }
        }

        Collections.sort(matches);
        return matches;
    }

    /**
     * Returns the number of registered commands.
     *
     * @return the command count
     */
    public int size() {
        return commands.size();
    }

    /**
     * Checks if the registry is empty.
     *
     * @return true if no commands are registered
     */
    public boolean isEmpty() {
        return commands.isEmpty();
    }

    /**
     * Clears all registered commands.
     */
    public void clear() {
        commands.clear();
        aliases.clear();
    }

    /**
     * Returns statistics about the registry.
     *
     * @return a map containing registry statistics
     */
    public Map<String, Object> getStatistics() {
        final Map<String, Object> stats = new HashMap<>();
        stats.put("commands", commands.size());
        stats.put("aliases", aliases.size());
        stats.put("total_handlers", commands.values().stream()
                .mapToInt(cmd -> cmd.getHandlers().size())
                .sum());
        return stats;
    }

    @Override
    public String toString() {
        return "CommandRegistry{" +
                "commands=" + commands.size() +
                ", aliases=" + aliases.size() +
                '}';
    }
}
