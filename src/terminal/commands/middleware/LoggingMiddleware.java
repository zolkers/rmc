package terminal.commands.middleware;

import terminal.Logger;
import terminal.commands.CommandContext;
import terminal.commands.CommandMiddleware;
public class LoggingMiddleware implements CommandMiddleware {

    private final boolean logArgs;
    public LoggingMiddleware(final boolean logArgs) {
        this.logArgs = logArgs;
    }

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
