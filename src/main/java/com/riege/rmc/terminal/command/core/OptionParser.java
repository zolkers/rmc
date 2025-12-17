package com.riege.rmc.terminal.command.core;

import java.util.*;

/**
 * Parses command-line style options and flags from arguments.
 * <p>
 * Supports:
 * - Options: --name value or -n value
 * - Flags: --flag or -f
 * - Quoted values: --reason "some text with spaces"
 * - Multiple formats: --key=value
 * </p>
 *
 * @author riege
 * @version 1.0
 */
public final class OptionParser {

    public static class ParseResult {
        private final List<String> positionalArgs;
        private final Map<String, String> options;
        private final Map<String, Integer> flagCounts;

        public ParseResult() {
            this.positionalArgs = new ArrayList<>();
            this.options = new HashMap<>();
            this.flagCounts = new HashMap<>();
        }

        public List<String> getPositionalArgs() {
            return positionalArgs;
        }

        public Map<String, String> getOptions() {
            return options;
        }

        public Map<String, Integer> getFlagCounts() {
            return flagCounts;
        }

        public String getOption(String name) {
            return options.get(name);
        }

        public String getOption(String name, String defaultValue) {
            return options.getOrDefault(name, defaultValue);
        }

        public boolean hasFlag(String name) {
            return flagCounts.containsKey(name);
        }

        public int getFlagCount(String name) {
            return flagCounts.getOrDefault(name, 0);
        }

        public boolean hasOption(String name) {
            return options.containsKey(name);
        }

        void incrementFlag(String name) {
            flagCounts.put(name, flagCounts.getOrDefault(name, 0) + 1);
        }
    }

    /**
     * Parses arguments into positional args, options, and flags.
     *
     * @param args the raw arguments
     * @return the parse result
     */
    public static ParseResult parse(final String[] args) {
        final ParseResult result = new ParseResult();

        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];

            if (arg.startsWith("--")) {
                final String optionPart = arg.substring(2);

                if (optionPart.contains("=")) {
                    final String[] parts = optionPart.split("=", 2);
                    result.options.put(parts[0], parts.length > 1 ? parts[1] : "");
                } else {
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        result.options.put(optionPart, args[i + 1]);
                        i++;
                    } else {
                        result.incrementFlag(optionPart);
                    }
                }
            } else if (arg.startsWith("-") && arg.length() > 1) {
                final String optionName = arg.substring(1);

                if (optionName.length() > 1 && !Character.isDigit(optionName.charAt(0))) {
                    boolean allSingleChar = true;
                    for (char c : optionName.toCharArray()) {
                        if (!Character.isLetter(c)) {
                            allSingleChar = false;
                            break;
                        }
                    }

                    if (allSingleChar) {
                        // Handle -vvv as three -v flags (counted)
                        for (char c : optionName.toCharArray()) {
                            result.incrementFlag(String.valueOf(c));
                        }
                        continue;
                    }
                }

                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    result.options.put(optionName, args[i + 1]);
                    i++;
                } else {
                    result.incrementFlag(optionName);
                }
            } else {
                result.positionalArgs.add(arg);
            }
        }

        return result;
    }

    /**
     * Parses quoted values in arguments.
     * <p>
     * Handles: --reason "some long text" as single value
     * </p>
     *
     * @param args the raw arguments
     * @return normalized arguments with quotes resolved
     */
    public static String[] normalizeQuotes(final String[] args) {
        final List<String> normalized = new ArrayList<>();
        StringBuilder currentQuoted = null;
        boolean inQuotes = false;

        for (String arg : args) {
            if (arg.startsWith("\"")) {
                inQuotes = true;
                currentQuoted = new StringBuilder(arg.substring(1));

                if (arg.endsWith("\"") && arg.length() > 1) {
                    normalized.add(currentQuoted.substring(0, currentQuoted.length() - 1));
                    currentQuoted = null;
                    inQuotes = false;
                }
            } else if (inQuotes) {
                currentQuoted.append(" ").append(arg);

                if (arg.endsWith("\"")) {
                    normalized.add(currentQuoted.substring(0, currentQuoted.length() - 1));
                    currentQuoted = null;
                    inQuotes = false;
                }
            } else {
                normalized.add(arg);
            }
        }

        if (inQuotes) {
            normalized.add(currentQuoted.toString());
        }

        return normalized.toArray(new String[0]);
    }

    /**
     * Parses arguments with quote normalization.
     *
     * @param args the raw arguments
     * @return the parse result
     */
    public static ParseResult parseWithQuotes(final String[] args) {
        return parse(normalizeQuotes(args));
    }
}
