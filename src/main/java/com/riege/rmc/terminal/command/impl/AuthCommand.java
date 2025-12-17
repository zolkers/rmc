package com.riege.rmc.terminal.command.impl;

import com.riege.rmc.api.RMCApi;
import com.riege.rmc.api.auth.AuthenticationResult;
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
    private final RMCApi api = RMCApi.getInstance();

    @Override
    @CommandHandler
    public void execute(CommandContext ctx) {
        msg(ctx, "");

        // Authenticate asynchronously
        api.auth().authenticateAsync(
            // Status updates
            message -> msg(ctx, message),

            // Result handler
            result -> handleAuthResult(ctx, result)
        );
    }

    private void handleAuthResult(CommandContext ctx, AuthenticationResult result) {
        msg(ctx, "");

        switch (result) {
            case AuthenticationResult.Success success -> {
                success(ctx, "Successfully authenticated!");
                success(ctx, "Username: " + success.profile().username());
                success(ctx, "UUID: " + success.profile().uuid());
                msg(ctx, "");
                success(ctx, "You can now connect to servers using: connect <server>");
            }

            case AuthenticationResult.AlreadyAuthenticated alreadyAuth -> {
                msg(ctx, "Already authenticated as " + alreadyAuth.profile().username()
                    + " (" + alreadyAuth.profile().uuid() + ")");
                msg(ctx, "Use 'logout' to disconnect first.");
            }

            case AuthenticationResult.Failure failure -> {
                error(ctx, failure.errorMessage());
            }

            case AuthenticationResult.InProgress inProgress -> {
                // Shouldn't happen in final result, but handle it
                info(ctx, inProgress.statusMessage());
            }
        }

        msg(ctx, "");
    }
}
