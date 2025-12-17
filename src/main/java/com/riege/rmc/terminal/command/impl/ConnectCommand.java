package com.riege.rmc.terminal.command.impl;

import com.riege.rmc.api.RMCApi;
import com.riege.rmc.api.connection.ConnectionResult;
import com.riege.rmc.terminal.command.annotations.Argument;
import com.riege.rmc.terminal.command.annotations.Command;
import com.riege.rmc.terminal.command.annotations.CommandHandler;
import com.riege.rmc.terminal.command.annotations.Flag;
import com.riege.rmc.terminal.command.core.BaseCommand;
import com.riege.rmc.terminal.command.core.CommandContext;

@Command(
    name = "connect",
    description = "Connect to a Minecraft server",
    usage = "connect <server[:port]> [-v|-vv|-vvv]",
    minArgs = 1
)
public final class ConnectCommand extends BaseCommand {
    private final RMCApi api = RMCApi.getInstance();

    @CommandHandler
    public void execute(
        CommandContext ctx,
        @Argument(name = "address", description = "Server address (host:port)") String address,
        @Flag(name = "v", description = "Verbose packet logging (repeatable: -v, -vv, -vvv)", repeatable = true) int verbosity
    ) {
        if (verbosity > 0) {
            String level = verbosity == 1 ? "basic" : verbosity == 2 ? "detailed" : "complete with hex dumps";
            info(ctx, "Verbose mode enabled (level " + verbosity + " - " + level + ")");
        }

        msg(ctx, "");

        // Connect asynchronously
        api.connection().connectAsync(
            address,
            verbosity,
            // Status updates
            message -> msg(ctx, message),

            // Result handler
            result -> handleConnectionResult(ctx, result)
        );
    }

    private void handleConnectionResult(CommandContext ctx, ConnectionResult result) {
        msg(ctx, "");

        switch (result) {
            case ConnectionResult.Success success -> {
                success(ctx, "You are now connected to the server!");
                info(ctx, "Server: " + success.serverAddress());
                info(ctx, "User: " + success.username());
            }

            case ConnectionResult.NotAuthenticated notAuth -> {
                error(ctx, "You must authenticate first. Use 'auth' command.");
            }

            case ConnectionResult.Failure failure -> {
                error(ctx, "Connection failed: " + failure.errorMessage());
                if (failure.cause() != null && failure.cause().getCause() != null) {
                    error(ctx, "Cause: " + failure.cause().getCause().getMessage());
                }
            }

            case ConnectionResult.InProgress inProgress -> {
                // Shouldn't happen in final result
                info(ctx, inProgress.statusMessage());
            }

            case ConnectionResult.Disconnected disconnected -> {
                warning(ctx, "Disconnected from " + disconnected.serverAddress());
            }
        }

        msg(ctx, "");
    }
}
