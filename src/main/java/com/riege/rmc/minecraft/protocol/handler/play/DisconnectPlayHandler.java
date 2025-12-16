package com.riege.rmc.minecraft.protocol.handler.play;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.VarInt;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class DisconnectPlayHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.payload()));

        int reasonLength = VarInt.readVarInt(data);
        byte[] reasonBytes = new byte[reasonLength];
        data.readFully(reasonBytes);
        String reason = new String(reasonBytes);

        connection.getLogger().accept("Disconnected: " + reason);
        connection.disconnect();
    }
}
