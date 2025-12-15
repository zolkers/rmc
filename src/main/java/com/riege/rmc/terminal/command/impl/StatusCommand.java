package com.riege.rmc.terminal.command.impl;

import com.riege.rmc.minecraft.SessionManager;
import com.riege.rmc.minecraft.microsoft.AuthenticatedProfile;
import com.riege.rmc.terminal.command.annotations.Command;
import com.riege.rmc.terminal.command.core.BaseCommand;
import com.riege.rmc.terminal.command.core.CommandContext;

import java.time.Duration;
import java.time.Instant;

@Command(
    name = "status",
    description = "Display current session status",
    aliases = {"info", "whoami"},
    usage = "status"
)
public final class StatusCommand extends BaseCommand {

    @Override
    public void execute(CommandContext ctx) {
        msg(ctx, "");
        msg(ctx, "=== Session Status ===");
        msg(ctx, "");

        if (!SessionManager.isAuthenticated()) {
            error(ctx, "Authentication: Not authenticated");
            msg(ctx, "");
            msg(ctx, "Use 'auth' to authenticate with Microsoft");
            return;
        }

        AuthenticatedProfile profile = SessionManager.getProfile();

        if (profile.isExpired()) {
            error(ctx, "Authentication: Expired");
        } else {
            success(ctx, "Authentication: Active");
        }

        msg(ctx, "");

        msg(ctx, "Profile Information:");
        msg(ctx, "  Username: " + profile.username());
        msg(ctx, "  UUID: " + profile.uuid());

        msg(ctx, "");

        Instant expiresAt = profile.expiresAt();
        Instant now = Instant.now();

        if (now.isBefore(expiresAt)) {
            Duration remaining = Duration.between(now, expiresAt);
            long hours = remaining.toHours();
            long minutes = remaining.toMinutesPart();

            success(ctx, "Token Status: Valid");
            msg(ctx, "  Expires in: " + hours + "h " + minutes + "m");
        } else {
            error(ctx, "Token Status: Expired");
            msg(ctx, "  Please re-authenticate with 'auth'");
        }

        msg(ctx, "");

        error(ctx, "Server Connection: Not connected");
        msg(ctx, "  Use 'connect <server>' to connect");

        msg(ctx, "");
    }
}
