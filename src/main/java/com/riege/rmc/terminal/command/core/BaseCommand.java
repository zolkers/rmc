package com.riege.rmc.terminal.command.core;


import com.riege.rmc.terminal.command.annotations.CommandHandler;
public abstract class BaseCommand {
    @CommandHandler
    public final void onCommand(CommandContext ctx) {
        execute(ctx);
    }

    public abstract void execute(CommandContext ctx);
    protected void msg(CommandContext ctx, String msg) {
        ctx.send(msg);
    }

    protected void error(CommandContext ctx, String msg) {
        ctx.error(msg);
    }
}