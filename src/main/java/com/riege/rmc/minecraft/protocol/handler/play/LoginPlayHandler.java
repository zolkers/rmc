package com.riege.rmc.minecraft.protocol.handler.play;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.VarInt;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public final class LoginPlayHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.payload()));

        int entityId = data.readInt();
        boolean isHardcore = data.readBoolean();

        int dimensionCount = VarInt.readVarInt(data);
        for (int i = 0; i < dimensionCount; i++) {
            int nameLength = VarInt.readVarInt(data);
            data.skipBytes(nameLength);
        }

        connection.getLogger().accept("Successfully joined the game!");
        connection.getLogger().accept("Entity ID: " + entityId + " | Hardcore: " + isHardcore);
    }
}
