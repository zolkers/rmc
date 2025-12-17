package com.riege.rmc.terminal.command.impl;

import com.riege.rmc.api.RMCApi;
import com.riege.rmc.terminal.command.annotations.Command;
import com.riege.rmc.terminal.command.annotations.CommandHandler;
import com.riege.rmc.terminal.command.core.BaseCommand;
import com.riege.rmc.terminal.command.core.CommandContext;

@Command(
    name = "logout",
    description = "Logout from Microsoft account",
    aliases = {"disconnect"},
    usage = "logout"
)
public final class LogoutCommand extends BaseCommand {
    private final RMCApi api = RMCApi.getInstance();

    @Override
    @CommandHandler
    public void execute(CommandContext ctx) {
        String username = api.auth().logout();

        if (username == null) {
            error(ctx, "You are not authenticated.");
            return;
        }

        success(ctx, "Logged out from " + username);
    }
}
