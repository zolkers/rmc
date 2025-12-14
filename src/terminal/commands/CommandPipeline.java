package terminal.commands;

import java.util.List;
import java.util.function.Supplier;

/**
 * Middleware execution pipeline for commands.
 * <p>
 * Executes a chain of middlewares in order, with the final
 * handler at the end. Each middleware can decide whether to
 * continue the chain or stop execution.
 * </p>
 *
 * @author riege
 * @version 1.0
 * @since 1.21.4
 */
public class CommandPipeline {

    private final List<CommandMiddleware> middlewares;

    /**
     * Creates a new pipeline with the given middlewares.
     *
     * @param middlewares the list of middlewares to execute
     */
    public CommandPipeline(final List<CommandMiddleware> middlewares) {
        this.middlewares = middlewares;
    }

    /**
     * Executes the pipeline with a final handler.
     *
     * @param context      the command context
     * @param finalHandler the final handler to execute after all middlewares
     * @return {@code true} if the entire pipeline executed successfully
     */
    public boolean execute(final CommandContext context,
                           final Supplier<Boolean> finalHandler) {
        return executeMiddleware(context, 0, finalHandler);
    }

    /**
     * Recursively executes middlewares in the chain.
     *
     * @param context      the command context
     * @param index        the current middleware index
     * @param finalHandler the final handler
     * @return {@code true} if execution was successful
     */
    private boolean executeMiddleware(final CommandContext context,
                                       final int index,
                                       final Supplier<Boolean> finalHandler) {
        // If we've executed all middlewares, run the final handler
        if (index >= middlewares.size()) {
            return finalHandler.get();
        }

        // Get current middleware
        final CommandMiddleware middleware = middlewares.get(index);

        // Execute middleware with next handler
        return middleware.handle(context, () ->
                executeMiddleware(context, index + 1, finalHandler)
        );
    }
}
