package com.riege.rmc.terminal.command.impl;

import com.riege.rmc.minecraft.SessionManager;
import com.riege.rmc.persistence.PersistenceManager;
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

    @Override
    @CommandHandler
    public void execute(CommandContext ctx) {
        if (!SessionManager.isAuthenticated()) {
            error(ctx, "You are not authenticated.");
            return;
        }

        String username = SessionManager.getProfile().username();
        SessionManager.clear();

        // Clear saved profile
        try {
            PersistenceManager.getInstance().clearProfile();
            msg(ctx, "Profile cleared from disk");
        } catch (Exception e) {
            msg(ctx, "Warning: Could not clear saved profile: " + e.getMessage());
        }

        msg(ctx, "Logged out from " + username);
    }
}
