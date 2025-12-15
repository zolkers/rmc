package com.riege.rmc.terminal.command.core;

import com.riege.rmc.terminal.command.annotations.SubCommand;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Holds information about a subcommand including nested subcommands.
 *
 * @author riege
 * @version 1.0
 */
public class SubCommandInfo {

    private final String name;
    private final SubCommand annotation;
    private final Method handler;
    private final Object instance;
    private final Map<String, SubCommandInfo> subCommands;
    private final List<String> path; // Full path like ["player", "inventory", "clear"]

    /**
     * Creates a new SubCommandInfo.
     *
     * @param name       the subcommand name
     * @param annotation the annotation
     * @param handler    the handler method (can be null for nested subcommands)
     * @param instance   the instance containing the handler
     * @param path       the full command path
     */
    public SubCommandInfo(
            final String name,
            final SubCommand annotation,
            final Method handler,
            final Object instance,
            final List<String> path
    ) {
        this.name = name;
        this.annotation = annotation;
        this.handler = handler;
        this.instance = instance;
        this.subCommands = new HashMap<>();
        this.path = new ArrayList<>(path);
    }

    /**
     * Adds a nested subcommand.
     *
     * @param subCommand the subcommand to add
     */
    public void addSubCommand(final SubCommandInfo subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
        for (final String alias : subCommand.getAnnotation().aliases()) {
            subCommands.put(alias.toLowerCase(), subCommand);
        }
    }

    /**
     * Finds a subcommand by name or alias.
     *
     * @param name the subcommand name
     * @return the subcommand or {@code null} if not found
     */
    public SubCommandInfo findSubCommand(final String name) {
        return subCommands.get(name.toLowerCase());
    }

    /**
     * Checks if this subcommand has nested subcommands.
     *
     * @return {@code true} if has subcommands
     */
    public boolean hasSubCommands() {
        return !subCommands.isEmpty();
    }

    /**
     * Gets the full command path as a string.
     *
     * @return the path string (e.g., "player inventory clear")
     */
    public String getPathString() {
        return String.join(" ", path);
    }

    /**
     * Gets the full command path as a slash command.
     *
     * @return the command string (e.g., "/player inventory clear")
     */
    public String getCommandString() {
        return "/" + getPathString();
    }

    // Getters

    public String getName() {
        return name;
    }

    public SubCommand getAnnotation() {
        return annotation;
    }

    public Method getHandler() {
        return handler;
    }

    public Object getInstance() {
        return instance;
    }

    public Map<String, SubCommandInfo> getSubCommands() {
        return Collections.unmodifiableMap(subCommands);
    }

    public List<String> getPath() {
        return Collections.unmodifiableList(path);
    }

    public boolean isEnabled() {
        return annotation.enabled();
    }

    @Override
    public String toString() {
        return "SubCommandInfo{" +
                "path=" + getPathString() +
                ", hasHandler=" + (handler != null) +
                ", subCommands=" + subCommands.size() +
                '}';
    }
}
