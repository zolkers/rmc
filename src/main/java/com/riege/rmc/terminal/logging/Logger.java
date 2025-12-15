package com.riege.rmc.terminal.logging;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Global singleton logger providing static access to main.java.com.riege.rmc.terminal logging functionality.
 * <p>
 * This class acts as a facade to a thread-safe {@link MessageLogger} instance,
 * providing convenient static methods for logging throughout the application.
 * The logger must be initialized via {@link #initialize()} or {@link #initialize(int)}
 * before use. All logging operations after initialization are thread-safe.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * // Initialize the global logger
 * Logger.initialize();
 *
 * // Log messages from anywhere in the application
 * Logger.info("Application started");
 * Logger.error("Connection failed");
 * Logger.success("Login completed");
 * }</pre>
 * </p>
 *
 * @author riege
 * @version 1.0
 */
public final class Logger {

    /**
     * The singleton MessageLogger instance, initialized lazily.
     * Uses AtomicReference for thread-safe lazy initialization.
     */
    private static final AtomicReference<MessageLogger> INSTANCE = new AtomicReference<>();

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private Logger() {
        throw new AssertionError("Logger class should not be instantiated");
    }

    /**
     * Initializes the global logger with default capacity.
     * <p>
     * This method should be called once at application startup before any
     * logging operations. Subsequent calls will have no effect.
     * </p>
     *
     * @return {@code true} if initialization was successful; {@code false} if already initialized
     */
    public static boolean initialize() {
        return initialize(1000);
    }

    /**
     * Initializes the global logger with the specified capacity.
     * <p>
     * This method should be called once at application startup before any
     * logging operations. Subsequent calls will have no effect.
     * </p>
     *
     * @param maxCapacity the maximum number of messages to retain
     * @return {@code true} if initialization was successful; {@code false} if already initialized
     * @throws IllegalArgumentException if maxCapacity is less than 1
     */
    public static boolean initialize(final int maxCapacity) {
        if (maxCapacity < 1) {
            throw new IllegalArgumentException("Max capacity must be at least 1");
        }
        return INSTANCE.compareAndSet(null, new MessageLogger(maxCapacity));
    }

    /**
     * Sets a custom MessageLogger instance.
     * <p>
     * This method is primarily intended for testing purposes, allowing
     * injection of mock logger instances.
     * </p>
     *
     * @param logger the MessageLogger instance to use
     * @throws IllegalArgumentException if logger is null
     */
    public static void setLogger(final MessageLogger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("Logger cannot be null");
        }
        INSTANCE.set(logger);
    }

    /**
     * Retrieves the underlying MessageLogger instance.
     * <p>
     * This method is primarily intended for advanced use cases or testing.
     * </p>
     *
     * @return the MessageLogger instance, or {@code null} if not initialized
     */
    public static MessageLogger getInstance() {
        return INSTANCE.get();
    }

    /**
     * Checks if the logger has been initialized.
     *
     * @return {@code true} if the logger is ready for use; {@code false} otherwise
     */
    public static boolean isInitialized() {
        return INSTANCE.get() != null;
    }

    /**
     * Ensures the logger is initialized, throwing an exception if not.
     *
     * @throws IllegalStateException if the logger has not been initialized
     */
    private static void ensureInitialized() {
        if (!isInitialized()) {
            throw new IllegalStateException(
                    "Logger has not been initialized. Call Logger.initialize() first.");
        }
    }

    /**
     * Logs a message with the specified type.
     *
     * @param content the message content
     * @param type    the message type
     * @throws IllegalStateException    if the logger is not initialized
     * @throws IllegalArgumentException if content or type is null
     */
    public static void log(final String content, final MessageType type) {
        ensureInitialized();
        INSTANCE.get().log(content, type);
    }

    /**
     * Logs an informational message.
     *
     * @param content the message content
     * @throws IllegalStateException    if the logger is not initialized
     * @throws IllegalArgumentException if content is null
     */
    public static void info(final String content) {
        ensureInitialized();
        INSTANCE.get().info(content);
    }

    /**
     * Logs an error message.
     *
     * @param content the error message content
     * @throws IllegalStateException    if the logger is not initialized
     * @throws IllegalArgumentException if content is null
     */
    public static void error(final String content) {
        ensureInitialized();
        INSTANCE.get().error(content);
    }

    /**
     * Logs an error message with exception details.
     * <p>
     * The exception message and stack trace are appended to the content.
     * </p>
     *
     * @param content   the error message content
     * @param throwable the exception to log
     * @throws IllegalStateException    if the logger is not initialized
     * @throws IllegalArgumentException if content is null
     */
    public static void error(final String content, final Throwable throwable) {
        ensureInitialized();
        final String fullMessage = throwable != null
                ? content + ": " + throwable.getClass().getSimpleName() + " - " + throwable.getMessage()
                : content;
        INSTANCE.get().error(fullMessage);
    }

    /**
     * Logs a success message.
     *
     * @param content the success message content
     * @throws IllegalStateException    if the logger is not initialized
     * @throws IllegalArgumentException if content is null
     */
    public static void success(final String content) {
        ensureInitialized();
        INSTANCE.get().success(content);
    }

    /**
     * Logs a warning message.
     *
     * @param content the warning message content
     * @throws IllegalStateException    if the logger is not initialized
     * @throws IllegalArgumentException if content is null
     */
    public static void warning(final String content) {
        ensureInitialized();
        INSTANCE.get().warning(content);
    }

    /**
     * Logs a debug message.
     *
     * @param content the debug message content
     * @throws IllegalStateException    if the logger is not initialized
     * @throws IllegalArgumentException if content is null
     */
    public static void debug(final String content) {
        ensureInitialized();
        INSTANCE.get().debug(content);
    }

    /**
     * Logs a generic message with default formatting.
     * <p>
     * Equivalent to calling {@code System.out.println()} but captured in the message queue.
     * </p>
     *
     * @param content the message content
     * @throws IllegalStateException    if the logger is not initialized
     * @throws IllegalArgumentException if content is null
     */
    public static void printLine(final String content) {
        ensureInitialized();
        INSTANCE.get().logPlain(content);
    }

    /**
     * Retrieves a snapshot of all current messages.
     *
     * @return an unmodifiable list of messages in chronological order
     * @throws IllegalStateException if the logger is not initialized
     */
    public static List<Message> getMessages() {
        ensureInitialized();
        return INSTANCE.get().getMessages();
    }

    /**
     * Retrieves the most recent N messages.
     *
     * @param count the number of recent messages to retrieve
     * @return an unmodifiable list of the most recent messages
     * @throws IllegalStateException    if the logger is not initialized
     * @throws IllegalArgumentException if count is negative
     */
    public static List<Message> getRecentMessages(final int count) {
        ensureInitialized();
        return INSTANCE.get().getRecentMessages(count);
    }

    /**
     * Returns the current number of messages in the queue.
     *
     * @return the message count
     * @throws IllegalStateException if the logger is not initialized
     */
    public static int size() {
        ensureInitialized();
        return INSTANCE.get().size();
    }

    /**
     * Checks if the message queue is empty.
     *
     * @return {@code true} if no messages are present; {@code false} otherwise
     * @throws IllegalStateException if the logger is not initialized
     */
    public static boolean isEmpty() {
        ensureInitialized();
        return INSTANCE.get().isEmpty();
    }

    /**
     * Clears all messages from the queue.
     *
     * @throws IllegalStateException if the logger is not initialized
     */
    public static void clear() {
        ensureInitialized();
        INSTANCE.get().clear();
    }

    /**
     * Resets the logger, clearing the singleton instance.
     * <p>
     * This method is primarily intended for testing purposes.
     * After calling this method, {@link #initialize()} must be called
     * again before logging operations can be performed.
     * </p>
     */
    public static void reset() {
        INSTANCE.set(null);
    }
}
