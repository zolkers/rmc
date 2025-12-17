package com.riege.rmc.terminal.command.impl;

import com.riege.rmc.api.RMCApi;
import com.riege.rmc.api.session.SessionInfo;
import com.riege.rmc.terminal.command.annotations.Command;
import com.riege.rmc.terminal.command.annotations.CommandHandler;
import com.riege.rmc.terminal.command.core.BaseCommand;
import com.riege.rmc.terminal.command.core.CommandContext;

@Command(
    name = "status",
    description = "Display current session status",
    aliases = {"info", "whoami"},
    usage = "status"
)
public final class StatusCommand extends BaseCommand {
    private final RMCApi api = RMCApi.getInstance();

    @Override
    @CommandHandler
    public void execute(CommandContext ctx) {
        SessionInfo info = api.session().getSessionInfo();

        msg(ctx, "");
        msg(ctx, "=== Session Status ===");
        msg(ctx, "");

        if (!info.authenticated()) {
            error(ctx, "Authentication: Not authenticated");
            msg(ctx, "");
            msg(ctx, "Use 'auth' to authenticate with Microsoft");
            return;
        }

        // Authentication status
        if (info.expired()) {
            error(ctx, "Authentication: Expired");
        } else {
            success(ctx, "Authentication: Active");
        }

        msg(ctx, "");

        // Profile information
        msg(ctx, "Profile Information:");
        msg(ctx, "  Username: " + info.username());
        msg(ctx, "  UUID: " + info.uuid());

        msg(ctx, "");

        // Token status
        if (!info.expired()) {
            long hours = info.remainingTime().toHours();
            long minutes = info.remainingTime().toMinutesPart();

            success(ctx, "Token Status: Valid");
            msg(ctx, "  Expires in: " + hours + "h " + minutes + "m");
        } else {
            error(ctx, "Token Status: Expired");
            msg(ctx, "  Please re-authenticate with 'auth'");
        }

        msg(ctx, "");

        // Connection status
        if (info.connected()) {
            success(ctx, "Server Connection: Connected");
        } else {
            error(ctx, "Server Connection: Not connected");
            msg(ctx, "  Use 'connect <server>' to connect");
        }

        msg(ctx, "");
    }
}
