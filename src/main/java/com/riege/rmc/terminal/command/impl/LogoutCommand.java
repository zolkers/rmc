package com.riege.rmc.terminal.command.impl;

import com.riege.rmc.minecraft.SessionManager;
import com.riege.rmc.terminal.command.annotations.Command;
import com.riege.rmc.terminal.command.core.BaseCommand;
import com.riege.rmc.terminal.command.core.CommandContext;

@Command(
    name = "logout",
    description = "Logout from Microsoft account",
    aliases = {"disconnect"},
    usage = "logout"
)
public class LogoutCommand extends BaseCommand {

    @Override
    public void execute(CommandContext ctx) {
        if (!SessionManager.isAuthenticated()) {
            msg(ctx, "You are not authenticated.");
            return;
        }

        String username = SessionManager.getProfile().username();
        SessionManager.clear();
        msg(ctx, "Logged out from " + username);
    }
}
