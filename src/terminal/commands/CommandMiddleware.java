package terminal.commands;

/**
 * Middleware interface for command execution pipeline.
 * <p>
 * Middlewares can intercept command execution to perform:
 * - Permission checks
 * - Input validation
 * - Logging
 * - Rate limiting
 * - Argument transformation
 * - Error handling
 * </p>
 *
 * <p>Example middleware:</p>
 * <pre>
 * public class LoggingMiddleware implements CommandMiddleware {
 *     public boolean handle(CommandContext context, NextHandler next) {
 *         Logger.info("Executing: " + context.getCommandName());
 *         boolean result = next.handle();
 *         Logger.info("Completed: " + context.getCommandName());
 *         return result;
 *     }
 * }
 * </pre>
 *
 * @author riege
 * @version 1.0
 * @since 1.21.4
 */
@FunctionalInterface
public interface CommandMiddleware {

    /**
     * Handles command execution with the ability to continue the chain.
     *
     * @param context the command context
     * @param next    the next handler in the chain
     * @return {@code true} if execution should continue, {@code false} to stop
     */
    boolean handle(CommandContext context, NextHandler next);

    /**
     * Functional interface for continuing the middleware chain.
     */
    @FunctionalInterface
    interface NextHandler {
        /**
         * Continues execution to the next middleware or final handler.
         *
         * @return {@code true} if successful
         */
        boolean handle();
    }
}
