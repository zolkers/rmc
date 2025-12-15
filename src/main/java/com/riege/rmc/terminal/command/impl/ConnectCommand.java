package com.riege.rmc.terminal.command.impl;

import com.riege.rmc.minecraft.SessionManager;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.terminal.command.annotations.Argument;
import com.riege.rmc.terminal.command.annotations.Command;
import com.riege.rmc.terminal.command.core.BaseCommand;
import com.riege.rmc.terminal.command.core.CommandContext;

@Command(
    name = "connect",
    description = "Connect to a Minecraft server",
    usage = "connect <server[:port]>"
)
public class ConnectCommand extends BaseCommand {

    @Override
    public void execute(CommandContext ctx) {
        if (!SessionManager.isAuthenticated()) {
            error(ctx, "You must authenticate first. Use 'auth' command.");
            return;
        }

        if (ctx.getArgs().length == 0) {
            error(ctx, "Usage: connect <server[:port]>");
            return;
        }

        String address = ctx.getArg(0, "");
        if (address.isEmpty()) {
            error(ctx, "Server address cannot be empty");
            return;
        }

        msg(ctx, "Username: " + SessionManager.getProfile().username());
        msg(ctx, "");

        Thread connectionThread = getThread(ctx, address);
        connectionThread.start();
    }

    private Thread getThread(CommandContext ctx, String address) {
        Thread connectionThread = new Thread(() -> {
            try {
                ServerConnection serverConn =
                        new ServerConnection(
                                address,
                                message -> msg(ctx, message)
                        );

                serverConn.connect(SessionManager.getProfile());

                msg(ctx, "");
                msg(ctx, "You are now connected to the server!");
                msg(ctx, "Note: Full gameplay support coming soon!");

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
