package com.riege.rmc.terminal.command.impl;

import com.riege.rmc.api.RMCApi;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.packets.play.ChatMessagePacket;
import com.riege.rmc.terminal.command.annotations.Command;
import com.riege.rmc.terminal.command.annotations.CommandHandler;
import com.riege.rmc.terminal.command.core.BaseCommand;
import com.riege.rmc.terminal.command.core.CommandContext;

import java.util.Optional;

@Command(
    name = "say",
    description = "Send a message or command to the Minecraft server",
    usage = "say <message>",
    aliases = {"chat", "msg"},
    minArgs = 1
)
public final class SayCommand extends BaseCommand {
    private final RMCApi api = RMCApi.getInstance();

    @CommandHandler
    public void execute(CommandContext ctx) {
        // Get the full message by joining all arguments
        String fullMessage = String.join(" ", ctx.getArgs());

        // Check if connected
        Optional<ServerConnection> serverConnectionOpt = api.connection().getCurrentConnection();
        if (serverConnectionOpt.isEmpty()) {
            ctx.error("Not connected to any server. Use 'connect <server>' first.");
            return;
        }

        ServerConnection serverConnection = serverConnectionOpt.get();
        MinecraftConnection connection = serverConnection.getConnection();

        if (connection == null || !connection.isConnected()) {
            ctx.error("Connection lost to server.");
            return;
        }

        try {
            // Send the chat message packet
            ChatMessagePacket chatPacket = new ChatMessagePacket(fullMessage);
            connection.sendPacket(chatPacket);

            // Don't show confirmation to avoid cluttering the chat
            // The server will echo the message back if it's a chat message
            // Commands typically don't get echoed
        } catch (Exception e) {
            ctx.error("Failed to send message: " + e.getMessage());
        }
    }
}
