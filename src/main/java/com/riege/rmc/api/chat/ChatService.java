package com.riege.rmc.api.chat;

import com.riege.rmc.minecraft.protocol.ServerConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing chat messages and events.
 * Provides event-driven API for receiving and sending chat messages.
 */
public final class ChatService {
    private final List<ChatListener> listeners = new CopyOnWriteArrayList<>();
    private final List<ChatMessage> messageHistory = Collections.synchronizedList(new ArrayList<>());
    private final int maxHistorySize;
    private volatile ServerConnection activeConnection;

    public ChatService() {
        this(100); // Default history size
    }

    public ChatService(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    /**
     * Add a chat listener to receive message events.
     * Listeners are called on the protocol thread when messages arrive.
     *
     * @param listener The listener to add
     */
    public void addListener(ChatListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a chat listener.
     *
     * @param listener The listener to remove
     * @return true if the listener was removed, false if it wasn't registered
     */
    public boolean removeListener(ChatListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Remove all registered listeners.
     */
    public void clearListeners() {
        listeners.clear();
    }

    /**
     * Get the number of registered listeners.
     */
    public int getListenerCount() {
        return listeners.size();
    }

    /**
     * Get chat message history.
     * Returns an immutable snapshot of recent messages.
     *
     * @return List of recent chat messages, most recent last
     */
    public List<ChatMessage> getHistory() {
        synchronized (messageHistory) {
            return List.copyOf(messageHistory);
        }
    }

    /**
     * Get the last N messages from history.
     *
     * @param count Number of messages to retrieve
     * @return List of recent messages, most recent last
     */
    public List<ChatMessage> getLastMessages(int count) {
        synchronized (messageHistory) {
            int size = messageHistory.size();
            int fromIndex = Math.max(0, size - count);
            return List.copyOf(messageHistory.subList(fromIndex, size));
        }
    }

    /**
     * Clear chat message history.
     */
    public void clearHistory() {
        messageHistory.clear();
    }

    /**
     * Send a chat message to the server.
     * Requires an active connection.
     *
     * @param message The message to send
     * @return CompletableFuture that completes when the message is sent
     * @throws IllegalStateException if not connected to a server
     */
    public CompletableFuture<Void> sendMessage(String message) {
        ServerConnection connection = activeConnection;
        if (connection == null || !connection.isConnected()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("Not connected to a server")
            );
        }

        return CompletableFuture.runAsync(() -> {
            try {
                connection.sendChatMessage(message);
            } catch (Exception e) {
                throw new RuntimeException("Failed to send chat message", e);
            }
        });
    }

    /**
     * Send a chat message synchronously.
     * Blocks until the message is sent.
     *
     * @param message The message to send
     * @throws IllegalStateException if not connected to a server
     * @throws Exception if sending fails
     */
    public void sendMessageSync(String message) throws Exception {
        ServerConnection connection = activeConnection;
        if (connection == null || !connection.isConnected()) {
            throw new IllegalStateException("Not connected to a server");
        }
        connection.sendChatMessage(message);
    }

    /**
     * Set the active server connection.
     * Called internally when connecting/disconnecting.
     *
     * @param connection The active connection, or null if disconnected
     */
    public void setConnection(ServerConnection connection) {
        this.activeConnection = connection;
    }

    /**
     * Get the active server connection.
     */
    public Optional<ServerConnection> getConnection() {
        return Optional.ofNullable(activeConnection);
    }

    /**
     * Check if currently connected to a server.
     */
    public boolean isConnected() {
        return activeConnection != null && activeConnection.isConnected();
    }

    /**
     * Notify all listeners of a new chat message.
     * This method is called internally by protocol handlers.
     *
     * @param message The chat message to dispatch
     */
    public void dispatchMessage(ChatMessage message) {
        // Add to history
        synchronized (messageHistory) {
            messageHistory.add(message);
            // Trim history if it exceeds max size
            while (messageHistory.size() > maxHistorySize) {
                messageHistory.remove(0);
            }
        }

        // Notify all listeners
        for (ChatListener listener : listeners) {
            try {
                listener.onMessage(message);
            } catch (Exception e) {
                // Notify listener of error, but don't let one bad listener break others
                try {
                    listener.onError(e);
                } catch (Exception ignored) {
                    // Listener's error handler failed, nothing we can do
                }
            }
        }
    }
}
