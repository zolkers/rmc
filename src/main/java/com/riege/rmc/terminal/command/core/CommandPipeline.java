package com.riege.rmc.terminal.command.core;

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
 */
public record CommandPipeline(List<CommandMiddleware> middlewares) {

    /**
     * Creates a new pipeline with the given middlewares.
     *
     * @param middlewares the list of middlewares to execute
     */
    public CommandPipeline {
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
        if (index >= middlewares.size()) {
            return finalHandler.get();
        }

        final CommandMiddleware middleware = middlewares.get(index);

        return middleware.handle(context, () ->
                executeMiddleware(context, index + 1, finalHandler)
        );
    }
}
