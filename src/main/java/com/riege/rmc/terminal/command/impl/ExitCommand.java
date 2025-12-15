package com.riege.rmc.terminal.command.impl;

import com.riege.rmc.terminal.command.core.BaseCommand;
import com.riege.rmc.terminal.command.core.CommandContext;
import com.riege.rmc.terminal.command.annotations.Command;

@Command(
    name = "exit",
    description = "Exits the terminal",
    aliases = {"quit", "stop"}
)
public final class ExitCommand extends BaseCommand {

    @Override
    public void execute(CommandContext ctx) {
        msg(ctx, "Exiting...");
        System.exit(0);
    }
}