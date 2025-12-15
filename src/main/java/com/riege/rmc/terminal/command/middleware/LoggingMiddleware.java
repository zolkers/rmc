package com.riege.rmc.terminal.command.middleware;

import com.riege.rmc.terminal.logging.Logger;
import com.riege.rmc.terminal.command.core.CommandContext;
import com.riege.rmc.terminal.command.core.CommandMiddleware;

@SuppressWarnings("unused")
public record LoggingMiddleware(boolean logArgs) implements CommandMiddleware {

    public LoggingMiddleware() {
        this(true);
    }

    @Override
    public boolean handle(final CommandContext context, final NextHandler next) {
        final long startTime = System.currentTimeMillis();

        if (logArgs && context.getArgs().length > 0) {
            Logger.debug("Executing command: " + context.getCommandName() +
                    " with args: " + String.join(", ", context.getArgs()));
        } else {
            Logger.debug("Executing command: " + context.getCommandName());
        }

        final boolean result = next.handle();

        final long duration = System.currentTimeMillis() - startTime;
        Logger.debug("Command " + context.getCommandName() +
                " completed in " + duration + "ms");

        return result;
    }
}
