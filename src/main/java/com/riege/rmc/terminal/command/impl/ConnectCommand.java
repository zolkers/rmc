package com.riege.rmc.terminal.command.impl;

import com.riege.rmc.minecraft.SessionManager;
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

        ServerAddress serverAddr = parseAddress(address);

        msg(ctx, "Connecting to " + serverAddr.host + ":" + serverAddr.port + "...");
        msg(ctx, "Username: " + SessionManager.getProfile().username());
        msg(ctx, "");

        // TODO: Implement actual server connection
        // This would require:
        // - TCP connection to server
        // - Handshake packet
        // - Login sequence
        // - Encryption handling
        // - Packet reading/writing

        error(ctx, "Server connection not yet implemented.");
        msg(ctx, "Coming soon: Full Minecraft protocol support!");
    }

    private ServerAddress parseAddress(String address) {
        if (address.contains(":")) {
            String[] parts = address.split(":", 2);
            try {
                int port = Integer.parseInt(parts[1]);
                return new ServerAddress(parts[0], port);
            } catch (NumberFormatException e) {
                return new ServerAddress(parts[0], 25565);
            }
        }
        return new ServerAddress(address, 25565);
    }

    private record ServerAddress(String host, int port) {}
}
