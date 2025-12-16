package com.riege.rmc.terminal.command.core;

public abstract class BaseCommand {
    /**
     * Default execute method for commands that don't use @CommandHandler with custom parameters.
     * Override this method if you want simple command execution without parameter injection.
     * If you use @CommandHandler with parameters, you don't need to override this.
     */
    public void execute(CommandContext ctx) {}

    protected void msg(CommandContext ctx, String msg) {
        ctx.send(msg);
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
}