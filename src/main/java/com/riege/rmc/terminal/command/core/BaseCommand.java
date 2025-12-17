package com.riege.rmc.terminal.command.core;

/**
 * Base class for all commands providing common utility methods.
 * <p>
 * Subclasses can use @CommandHandler with parameter injection for cleaner code.
 * </p>
 */
public abstract class BaseCommand {
    /**
     * Default execute method for commands that don't use @CommandHandler with custom parameters.
     * Override this method if you want simple command execution without parameter injection.
     * If you use @CommandHandler with parameters, you don't need to override this.
     */
    public void execute(CommandContext ctx) {}

    // ========== Message Helper Methods ==========

    protected void msg(CommandContext ctx, String msg) {
        ctx.send(msg);
    }

    protected void info(CommandContext ctx, String msg) {
        ctx.info(msg);
    }

    protected void error(CommandContext ctx, String msg) {
        ctx.error(msg);
    }

    protected void success(CommandContext ctx, String msg) {
        ctx.success(msg);
    }

    protected void warning(CommandContext ctx, String msg) {
        ctx.warning(msg);
    }

    protected void debug(CommandContext ctx, String msg) {
        ctx.debug(msg);
    }

    // ========== Argument Helper Methods ==========

    protected String getArg(CommandContext ctx, int index) {
        return ctx.getArg(index);
    }

    protected String getArg(CommandContext ctx, int index, String defaultValue) {
        return ctx.getArg(index, defaultValue);
    }

    protected int getArgAsInt(CommandContext ctx, int index, int defaultValue) {
        return ctx.getArgAsInt(index, defaultValue);
    }

    protected boolean hasMinArgs(CommandContext ctx, int count) {
        return ctx.hasMinArgs(count);
    }

    // ========== Option Helper Methods ==========

    protected String getOption(CommandContext ctx, String name) {
        return ctx.getOption(name);
    }

    protected String getOption(CommandContext ctx, String name, String defaultValue) {
        return ctx.getOption(name, defaultValue);
    }

    protected int getOptionAsInt(CommandContext ctx, String name, int defaultValue) {
        return ctx.getOptionAsInt(name, defaultValue);
    }

    protected boolean hasOption(CommandContext ctx, String name) {
        return ctx.hasOption(name);
    }

    // ========== Flag Helper Methods ==========

    protected boolean hasFlag(CommandContext ctx, String name) {
        return ctx.hasFlag(name);
    }

    protected int getFlagCount(CommandContext ctx, String name) {
        return ctx.getFlagCount(name);
    }

    /**
     * Gets the verbosity level from -v, -vv, -vvv flags.
     * Useful for commands that support multiple verbosity levels.
     *
     * @param ctx the command context
     * @return 0 for no flags, 1 for -v, 2 for -vv, 3 for -vvv, etc.
     */
    protected int getVerbosity(CommandContext ctx) {
        return ctx.getFlagCount("v");
    }
}