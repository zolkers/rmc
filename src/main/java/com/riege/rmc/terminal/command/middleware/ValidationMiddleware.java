package com.riege.rmc.terminal.command.middleware;

import com.riege.rmc.terminal.command.core.CommandContext;
import com.riege.rmc.terminal.command.core.CommandMiddleware;
import com.riege.rmc.terminal.command.annotations.Command;

@SuppressWarnings("unused")
public final class ValidationMiddleware implements CommandMiddleware {

    @Override
    public boolean handle(final CommandContext context, final NextHandler next) {
        final Object commandInstance = context.getData("commandInstance");
        if (commandInstance == null) {
            return next.handle();
        }

        final Class<?> clazz = commandInstance.getClass();
        final Command annotation = clazz.getAnnotation(Command.class);

        if (annotation != null && !annotation.usage().isEmpty()) {
            final String usage = annotation.usage();
            final int requiredArgs = countRequiredArgs(usage);

            if (context.getArgs().length < requiredArgs) {
                context.error("Invalid arguments. Usage: " + usage);
                return false;
            }
        }

        return next.handle();
    }

    /**
     * Counts required arguments from usage string.
     * Required args are in angle brackets: <arg>
     * Optional args are in square brackets: [arg]
     *
     * @param usage the usage string
     * @return the number of required arguments
     */
    private int countRequiredArgs(final String usage) {
        int count = 0;
        boolean inRequired = false;

        for (char c : usage.toCharArray()) {
            if (c == '<') {
                inRequired = true;
            } else if (c == '>' && inRequired) {
                count++;
                inRequired = false;
            }
        }

        return count;
    }
}
