package com.riege.rmc.api.chat;

/**
 * Listener interface for chat events.
 * Implement this interface to receive chat messages from the server.
 */
@FunctionalInterface
public interface ChatListener {
    /**
     * Called when a chat message is received from the server.
     *
     * @param message The received chat message
     */
    void onMessage(ChatMessage message);

    /**
     * Called when a chat-related error occurs.
     * Default implementation does nothing.
     *
     * @param error The error that occurred
     */
    default void onError(Throwable error) {
        // Default: do nothing
    }
}
