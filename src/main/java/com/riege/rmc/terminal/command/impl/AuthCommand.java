package com.riege.rmc.terminal.command.impl;

import com.riege.rmc.minecraft.SessionManager;
import com.riege.rmc.minecraft.microsoft.AuthException;
import com.riege.rmc.minecraft.microsoft.AuthenticatedProfile;
import com.riege.rmc.minecraft.microsoft.MinecraftAuth;
import com.riege.rmc.minecraft.microsoft.MicrosoftAuth;
import com.riege.rmc.minecraft.microsoft.MicrosoftToken;
import com.riege.rmc.persistence.PersistenceManager;
import com.riege.rmc.terminal.command.annotations.Command;
import com.riege.rmc.terminal.command.annotations.CommandHandler;
import com.riege.rmc.terminal.command.core.BaseCommand;
import com.riege.rmc.terminal.command.core.CommandContext;

@Command(
    name = "auth",
    description = "Authenticate with Microsoft account",
    aliases = {"login"},
    usage = "auth"
)
public final class AuthCommand extends BaseCommand {

    @Override
    @CommandHandler
    public void execute(CommandContext ctx) {
        if (SessionManager.isAuthenticated()) {
            AuthenticatedProfile profile = SessionManager.getProfile();
            msg(ctx, "Already authenticated as " + profile.username() + " (" + profile.uuid() + ")");
            msg(ctx, "Use 'logout' to disconnect first.");
            return;
        }

        msg(ctx, "Starting Microsoft authentication...");
        msg(ctx, "");

        Thread authThread = new Thread(() -> {
            try {
                MicrosoftAuth msAuth = new MicrosoftAuth();
                MicrosoftToken msToken = msAuth.authenticate(message -> msg(ctx, message));

                msg(ctx, "");
                msg(ctx, "Authenticating with Minecraft services...");

                MinecraftAuth mcAuth = new MinecraftAuth();
                AuthenticatedProfile profile = mcAuth.authenticateWithMicrosoft(msToken);

                SessionManager.setProfile(profile);

                // Save profile to disk
                try {
                    PersistenceManager.getInstance().saveProfile(profile);
                    msg(ctx, "Profile saved to disk");
                } catch (Exception saveEx) {
                    msg(ctx, "Warning: Could not save profile: " + saveEx.getMessage());
                }

                msg(ctx, "");
                success(ctx, "Successfully authenticated!");
                success(ctx, "Username: " + profile.username());
                success(ctx, "UUID: " + profile.uuid());
                msg(ctx, "");
                success(ctx, "You can now connect to servers using: connect <server>");

            } catch (AuthException e) {
                error(ctx, "Authentication failed: " + e.getMessage());
            } catch (Exception e) {
                error(ctx, "Unexpected error: " + e.getMessage());
                System.err.println(e.getMessage());
            }
        });

        authThread.setDaemon(true);
        authThread.start();
    }
}
