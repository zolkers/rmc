package com.riege.rmc.minecraft.protocol.handler.configuration;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.PacketBuffer;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;
import com.riege.rmc.minecraft.protocol.packets.configuration.PongConfigPacket;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Handles Ping packet from server.
 * Server packet ID: 0x05 in CONFIGURATION state
 */
public class PingConfigHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        DataInputStream data = packet.getDataStream();
        int id = data.readInt();

        connection.getLogger().accept("Received ping, responding...");

        // Send pong response
        PongConfigPacket pong = new PongConfigPacket(id);
        connection.getConnection().sendPacket(pong);
    }
}
