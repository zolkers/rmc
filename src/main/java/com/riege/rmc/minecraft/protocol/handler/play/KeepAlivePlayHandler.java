package com.riege.rmc.minecraft.protocol.handler.play;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;
import com.riege.rmc.minecraft.protocol.packets.play.KeepAlivePlayPacket;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class KeepAlivePlayHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.payload()));
        long keepAliveId = data.readLong();

        if (connection.getKeepAliveManager() != null) {
            connection.getKeepAliveManager().recordKeepAlive(keepAliveId);
        }

        // Respond with the same keep-alive ID
        KeepAlivePlayPacket response = new KeepAlivePlayPacket(keepAliveId);
        connection.getConnection().sendPacket(response);
    }
}
