package com.riege.rmc.terminal.command.core;

import com.riege.rmc.terminal.logging.Logger;

import java.util.*;

/**
 * Execution context for command handlers.
 * <p>
 * This class provides command handlers with access to execution information
 * including arguments, sender details, and convenience methods for common
 * operations like argument parsing and message sending.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * @CommandHandler
 * public void execute(CommandContext ctx) {
 *     // Get arguments
 *     String player = ctx.getArg(0, "default");
 *     int amount = ctx.getArgAsInt(1, 1);
 *
 *     // Send messages
 *     ctx.info("Processing command...");
 *     ctx.success("Gave " + amount + " items to " + player);
 *
 *     // Check argument count
 *     if (ctx.getArgCount() < 2) {
 *         ctx.error("Not enough arguments!");
 *         ctx.sendUsage();
 *         return;
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @author riege
 * @version 1.0
 */
public class CommandContext {

    /**
     * The name of the command being executed.
     */
    private final String commandName;

    /**
     * The command arguments (excluding the command name itself).
     */
    private final String[] args;

    /**
     * The original raw input string.
     */
    private final String rawInput;

    /**
     * The sender who executed the command.
     */
    private final String sender;

    /**
     * Custom data storage for passing information between handlers.
     */
    private final Map<String, Object> data;

    /**
     * Parsed options and flags.
     */
    private final OptionParser.ParseResult options;

    /**
     * Usage string for this command.
     */
    private String usage;

    /**
     * Constructs a CommandContext with the specified parameters.
     *
     * @param commandName the command name
     * @param args        the command arguments
     * @param rawInput    the raw input string
     * @param sender      the command sender
     */
    public CommandContext(
            final String commandName,
            final String[] args,
            final String rawInput,
            final String sender
    ) {
        this.commandName = commandName;
        this.rawInput = rawInput;
        this.sender = sender;
        this.data = new HashMap<>();
        this.usage = "";

        // Parse options and flags from args
        this.options = OptionParser.parseWithQuotes(args != null ? args : new String[0]);

        // Use positional args as the actual args
        this.args = this.options.getPositionalArgs().toArray(new String[0]);
    }

    // ========== Argument Access Methods ==========

    /**
     * Returns the number of arguments.
     *
     * @return the argument count
     */
    public int getArgCount() {
        return args.length;
    }

    /**
     * Returns all arguments as an array.
     *
     * @return the arguments array
     */
    public String[] getArgs() {
        return args.clone();
    }

    /**
     * Returns all arguments as a list.
     *
     * @return unmodifiable list of arguments
     */
    public List<String> getArgsList() {
        return Arrays.asList(args);
    }

    /**
     * Retrieves an argument at the specified index.
     *
     * @param index the argument index (0-based)
     * @return Optional containing the argument, or empty if index is out of bounds
     */
    public Optional<String> getArgOptional(final int index) {
        if (index >= 0 && index < args.length) {
            return Optional.of(args[index]);
        }
        return Optional.empty();
    }

    /**
     * Retrieves an argument at the specified index, or returns a default value.
     *
     * @param index        the argument index (0-based)
     * @param defaultValue the default value if index is out of bounds
     * @return the argument or default value
     */
    public String getArg(final int index, final String defaultValue) {
        return getArgOptional(index).orElse(defaultValue);
    }

    /**
     * Retrieves an argument at the specified index.
     *
     * @param index the argument index (0-based)
     * @return the argument value
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public String getArg(final int index) {
        if (index < 0 || index >= args.length) {
            throw new IndexOutOfBoundsException(
                    "Argument index " + index + " out of bounds for length " + args.length);
        }
        return args[index];
    }

    /**
     * Retrieves an argument as an integer.
     *
     * @param index        the argument index
     * @param defaultValue the default value if parsing fails
     * @return the parsed integer or default value
     */
    public int getArgAsInt(final int index, final int defaultValue) {
        return getArgOptional(index)
                .map(arg -> {
                    try {
                        return Integer.parseInt(arg);
                    } catch (final NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    /**
     * Retrieves an argument as a long.
     *
     * @param index        the argument index
     * @param defaultValue the default value if parsing fails
     * @return the parsed long or default value
     */
    public long getArgAsLong(final int index, final long defaultValue) {
        return getArgOptional(index)
                .map(arg -> {
                    try {
                        return Long.parseLong(arg);
                    } catch (final NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    /**
     * Retrieves an argument as a double.
     *
     * @param index        the argument index
     * @param defaultValue the default value if parsing fails
     * @return the parsed double or default value
     */
    public double getArgAsDouble(final int index, final double defaultValue) {
        return getArgOptional(index)
                .map(arg -> {
                    try {
                        return Double.parseDouble(arg);
                    } catch (final NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    /**
     * Retrieves an argument as a boolean.
     * <p>
     * Accepts: "true", "yes", "1", "on" for true; "false", "no", "0", "off" for false.
     * </p>
     *
     * @param index        the argument index
     * @param defaultValue the default value if parsing fails
     * @return the parsed boolean or default value
     */
    public boolean getArgAsBoolean(final int index, final boolean defaultValue) {
        return getArgOptional(index)
                .map(arg -> {
                    final String lower = arg.toLowerCase();
                    if ("true".equals(lower) || "yes".equals(lower) || "1".equals(lower) || "on".equals(lower)) {
                        return true;
                    } else if ("false".equals(lower) || "no".equals(lower) || "0".equals(lower) || "off".equals(lower)) {
                        return false;
                    }
                    return defaultValue;
                })
                .orElse(defaultValue);
    }

    /**
     * Joins arguments from a starting index to the end.
     *
     * @param startIndex the starting index (inclusive)
     * @return the joined string
     */
    public String joinArgs(final int startIndex) {
        return joinArgs(startIndex, args.length);
    }

    /**
     * Joins arguments from a starting index to an ending index.
     *
     * @param startIndex the starting index (inclusive)
     * @param endIndex   the ending index (exclusive)
     * @return the joined string
     */
    public String joinArgs(final int startIndex, final int endIndex) {
        if (startIndex < 0 || startIndex >= args.length) {
            return "";
        }
        final int end = Math.min(endIndex, args.length);
        return String.join(" ", Arrays.copyOfRange(args, startIndex, end));
    }

    // ========== Message Methods ==========

    /**
     * Sends an info message to the command sender.
     *
     * @param message the message to send
     */
    public void info(final String message) {
        Logger.info(message);
    }

    /**
     * Sends an error message to the command sender.
     *
     * @param message the error message
     */
    public void error(final String message) {
        Logger.error(message);
    }

    /**
     * Sends a success message to the command sender.
     *
     * @param message the success message
     */
    public void success(final String message) {
        Logger.success(message);
    }

    /**
     * Sends a warning message to the command sender.
     *
     * @param message the warning message
     */
    public void warning(final String message) {
        Logger.warning(message);
    }

    /**
     * Sends a debug message to the command sender.
     *
     * @param message the debug message
     */
    public void debug(final String message) {
        Logger.debug(message);
    }

    /**
     * Sends a plain message to the command sender.
     *
     * @param message the message
     */
    public void send(final String message) {
        Logger.printLine(message);
    }

    /**
     * Sends the usage message for this command.
     */
    public void sendUsage() {
        if (usage != null && !usage.isEmpty()) {
            error("Usage: " + usage);
        } else {
            error("Usage: /" + commandName + " [args...]");
        }
    }

    // ========== Context Information Methods ==========

    /**
     * Returns the command name.
     *
     * @return the command name
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * Returns the raw input string.
     *
     * @return the raw input
     */
    public String getRawInput() {
        return rawInput;
    }

    /**
     * Returns the command sender identifier.
     *
     * @return the sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * Sets the usage string for this command.
     *
     * @param usage the usage string
     */
    public void setUsage(final String usage) {
        this.usage = usage;
    }

    /**
     * Returns the usage string.
     *
     * @return the usage string
     */
    public String getUsage() {
        return usage;
    }

    // ========== Data Storage Methods ==========

    /**
     * Stores a custom data value.
     *
     * @param key   the data key
     * @param value the data value
     */
    public void setData(final String key, final Object value) {
        data.put(key, value);
    }

    /**
     * Retrieves a custom data value.
     *
     * @param key the data key
     * @param <T> the expected value type
     * @return Optional containing the value, or empty if not found
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getData(final String key) {
        return Optional.ofNullable((T) data.get(key));
    }

    /**
     * Retrieves a custom data value with a default.
     *
     * @param key          the data key
     * @param defaultValue the default value
     * @param <T>          the expected value type
     * @return the value or default
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(final String key, final T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    /**
     * Checks if a data key exists.
     *
     * @param key the data key
     * @return true if the key exists
     */
    public boolean hasData(final String key) {
        return data.containsKey(key);
    }

    /**
     * Removes a data value.
     *
     * @param key the data key
     */
    public void removeData(final String key) {
        data.remove(key);
    }

    /**
     * Clears all custom data.
     */
    public void clearData() {
        data.clear();
    }

    // ========== Option and Flag Methods ==========

    /**
     * Gets an option value.
     *
     * @param name the option name
     * @return the option value or {@code null} if not present
     */
    public String getOption(final String name) {
        return options.getOption(name);
    }

    /**
     * Gets an option value with a default.
     *
     * @param name         the option name
     * @param defaultValue the default value
     * @return the option value or default
     */
    public String getOption(final String name, final String defaultValue) {
        return options.getOption(name, defaultValue);
    }

    /**
     * Gets an option as an integer.
     *
     * @param name         the option name
     * @param defaultValue the default value
     * @return the parsed integer or default
     */
    public int getOptionAsInt(final String name, final int defaultValue) {
        final String value = options.getOption(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets an option as a long.
     *
     * @param name         the option name
     * @param defaultValue the default value
     * @return the parsed long or default
     */
    public long getOptionAsLong(final String name, final long defaultValue) {
        final String value = options.getOption(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets an option as a double.
     *
     * @param name         the option name
     * @param defaultValue the default value
     * @return the parsed double or default
     */
    public double getOptionAsDouble(final String name, final double defaultValue) {
        final String value = options.getOption(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets an option as a boolean.
     *
     * @param name         the option name
     * @param defaultValue the default value
     * @return the parsed boolean or default
     */
    public boolean getOptionAsBoolean(final String name, final boolean defaultValue) {
        final String value = options.getOption(name);
        if (value == null) {
            return defaultValue;
        }
        final String lower = value.toLowerCase();
        if ("true".equals(lower) || "yes".equals(lower) || "1".equals(lower) || "on".equals(lower)) {
            return true;
        } else if ("false".equals(lower) || "no".equals(lower) || "0".equals(lower) || "off".equals(lower)) {
            return false;
        }
        return defaultValue;
    }

    /**
     * Checks if an option is present.
     *
     * @param name the option name
     * @return {@code true} if the option exists
     */
    public boolean hasOption(final String name) {
        return options.hasOption(name);
    }

    /**
     * Checks if a flag is present.
     *
     * @param name the flag name
     * @return {@code true} if the flag is present
     */
    public boolean hasFlag(final String name) {
        return options.hasFlag(name);
    }

    /**
     * Gets all options.
     *
     * @return unmodifiable map of options
     */
    public Map<String, String> getAllOptions() {
        return Collections.unmodifiableMap(options.getOptions());
    }

    /**
     * Gets all flags.
     *
     * @return unmodifiable set of flags
     */
    public Set<String> getAllFlags() {
        return Collections.unmodifiableSet(options.getFlags());
    }

    // ========== Utility Methods ==========

    /**
     * Checks if there are at least N arguments.
     *
     * @param count the required argument count
     * @return true if enough arguments are present
     */
    public boolean hasMinArgs(final int count) {
        return args.length >= count;
    }

    /**
     * Validates minimum argument count, sending error if insufficient.
     *
     * @param count the required argument count
     * @return true if validation passes
     */
    public boolean requireMinArgs(final int count) {
        if (!hasMinArgs(count)) {
            error("Not enough arguments! Expected at least " + count + ", got " + args.length);
            sendUsage();
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CommandContext{" +
                "command='" + commandName + '\'' +
                ", args=" + Arrays.toString(args) +
                ", sender='" + sender + '\'' +
                '}';
    }
}
