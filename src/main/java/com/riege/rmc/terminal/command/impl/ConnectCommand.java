package com.riege.rmc.terminal.command.impl;

import com.riege.rmc.minecraft.SessionManager;
import com.riege.rmc.minecraft.protocol.ServerConnection;
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

    @CommandHandler
    public void execute(
        CommandContext ctx,
        @Argument(name = "address", description = "Server address (host:port)") String address,
        @Flag(name = "vvv", description = "Extremely verbose packet logging with hex dumps") boolean vvv,
        @Flag(name = "vv", description = "Very verbose packet logging with details") boolean vv,
        @Flag(name = "v", description = "Basic verbose packet logging") boolean v
    ) {
        if (!SessionManager.isAuthenticated()) {
            error(ctx, "You must authenticate first. Use 'auth' command.");
            return;
        }

        int verbosity = vvv ? 3 : vv ? 2 : v ? 1 : 0;

        if (verbosity > 0) {
            msg(ctx, "Verbose mode enabled (level " + verbosity + ")");
        }

        msg(ctx, "Username: " + SessionManager.getProfile().username());
        msg(ctx, "");

        Thread connectionThread = createConnectionThread(ctx, address, verbosity);
        connectionThread.start();
    }

    private Thread createConnectionThread(CommandContext ctx, String address, int verbosity) {
        Thread connectionThread = new Thread(() -> {
            try {
                ServerConnection serverConn =
                        new ServerConnection(
                                address,
                                message -> msg(ctx, message),
                                verbosity
                        );

                serverConn.connect(SessionManager.getProfile());

                msg(ctx, "");
                success(ctx, "You are now connected to the server!");

            } catch (Exception e) {
                error(ctx, "Connection failed: " + e.getMessage());
                if (e.getCause() != null) {
                    error(ctx, "Cause: " + e.getCause().getMessage());
                }
            }
        });

        connectionThread.setDaemon(true);
        return connectionThread;
    }
}
