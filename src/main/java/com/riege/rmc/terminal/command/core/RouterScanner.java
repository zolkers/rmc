package com.riege.rmc.terminal.command.core;

import com.riege.rmc.terminal.logging.Logger;
import com.riege.rmc.terminal.command.annotations.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Scans and processes @Router annotations to automatically register commands.
 *
 * @author riege
 * @version 1.0
 */
public class RouterScanner {

    /**
     * Scans a router class and extracts all commands and subcommands.
     *
     * @param routerClass the router class
     * @return list of command instances to register
     */
    public List<Object> scanRouter(final Class<?> routerClass) {
        final Router routerAnnotation = routerClass.getAnnotation(Router.class);
        if (routerAnnotation == null) {
            throw new IllegalArgumentException(
                    "Class " + routerClass.getName() + " is not annotated with @Router");
        }

        if (!routerAnnotation.enabled()) {
            Logger.debug("Skipping disabled router: " + routerAnnotation.name());
            return Collections.emptyList();
        }

        final List<Object> commands = new ArrayList<>();

        for (final Class<?> innerClass : routerClass.getDeclaredClasses()) {
            final Command commandAnnotation = innerClass.getAnnotation(Command.class);
            if (commandAnnotation != null && commandAnnotation.enabled()) {
                try {
                    Object commandInstance;
                    if (Modifier.isStatic(innerClass.getModifiers())) {
                        commandInstance = innerClass.getDeclaredConstructor().newInstance();
                    } else {
                        Object routerInstance = routerClass.getDeclaredConstructor().newInstance();
                        commandInstance = innerClass.getDeclaredConstructor(routerClass)
                                .newInstance(routerInstance);
                    }

                    commands.add(commandInstance);
                    Logger.debug("Found command in router: " + commandAnnotation.name());
                } catch (Exception e) {
                    Logger.warning("Failed to instantiate command " + innerClass.getName() +
                            ": " + e.getMessage());
                }
            }
        }

        return commands;
    }

    /**
     * Scans a command class for subcommands.
     *
     * @param commandClass the command class
     * @param instance     the command instance
     * @param basePath     the base command path
     * @return map of subcommand paths to info
     */
    public Map<String, SubCommandInfo> scanSubCommands(
            final Class<?> commandClass,
            final Object instance,
            final List<String> basePath
    ) {
        final Map<String, SubCommandInfo> subCommands = new HashMap<>();

        for (final Method method : commandClass.getDeclaredMethods()) {
            final SubCommand subCommandAnnotation = method.getAnnotation(SubCommand.class);
            if (subCommandAnnotation != null && subCommandAnnotation.enabled()) {
                final List<String> path = new ArrayList<>(basePath);
                path.add(subCommandAnnotation.name());

                final SubCommandInfo info = new SubCommandInfo(
                        subCommandAnnotation.name(),
                        subCommandAnnotation,
                        method,
                        instance,
                        path
                );

                subCommands.put(String.join(" ", path), info);
                Logger.debug("Found subcommand: /" + info.getPathString());
            }
        }

        for (final Class<?> innerClass : commandClass.getDeclaredClasses()) {
            final SubCommand subCommandAnnotation = innerClass.getAnnotation(SubCommand.class);
            if (subCommandAnnotation != null && subCommandAnnotation.enabled()) {
                final List<String> path = new ArrayList<>(basePath);
                path.add(subCommandAnnotation.name());

                try {
                    Object subInstance;
                    if (Modifier.isStatic(innerClass.getModifiers())) {
                        subInstance = innerClass.getDeclaredConstructor().newInstance();
                    } else {
                        subInstance = innerClass.getDeclaredConstructor(commandClass)
                                .newInstance(instance);
                    }

                    final SubCommandInfo info = new SubCommandInfo(
                            subCommandAnnotation.name(),
                            subCommandAnnotation,
                            null,
                            subInstance,
                            path
                    );

                    final Map<String, SubCommandInfo> nestedSubs =
                            scanSubCommands(innerClass, subInstance, path);

                    for (final SubCommandInfo nested : nestedSubs.values()) {
                        info.addSubCommand(nested);
                    }

                    subCommands.put(String.join(" ", path), info);
                    subCommands.putAll(nestedSubs);

                    Logger.debug("Found nested subcommand: /" + info.getPathString() +
                            " with " + info.getSubCommands().size() + " child(ren)");
                } catch (Exception e) {
                    Logger.warning("Failed to instantiate subcommand " + innerClass.getName() +
                            ": " + e.getMessage());
                }
            }
        }

        return subCommands;
    }
}
