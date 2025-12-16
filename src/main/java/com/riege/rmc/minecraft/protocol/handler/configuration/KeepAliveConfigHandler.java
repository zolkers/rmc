package com.riege.rmc.minecraft.protocol.handler.configuration;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;
import com.riege.rmc.minecraft.protocol.packets.configuration.KeepAliveConfigPacket;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class KeepAliveConfigHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.payload()));
        long keepAliveId = data.readLong();

        connection.getKeepAliveManager().recordKeepAlive(keepAliveId);

        // Respond with the same keep-alive ID
        KeepAliveConfigPacket response = new KeepAliveConfigPacket(keepAliveId);
        connection.getConnection().sendPacket(response);
    }
}
