package com.riege.rmc.minecraft.protocol.handler.configuration;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.VarInt;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Handles Custom Payload packet from server.
 * Server packet ID: 0x01 in CONFIGURATION state
 *
 * Custom payloads are plugin messages - we just log and ignore them.
 */
public class CustomPayloadHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        DataInputStream data = packet.getDataStream();

        // Read channel name
        int channelLength = VarInt.readVarInt(data);
        byte[] channelBytes = new byte[channelLength];
        data.readFully(channelBytes);
        String channel = new String(channelBytes);

        connection.getLogger().accept("Received custom payload: " + channel);
        // Payload data is consumed by reading the packet, we just ignore it
    }
}
