package com.riege.rmc.minecraft.protocol.handler.play;

import com.riege.rmc.minecraft.nbt.NBT;
import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.VarInt;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;

import java.io.DataInputStream;
import java.io.IOException;

public final class DisconnectPlayHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        DataInputStream data = packet.getDataStream();

        int reasonLength = VarInt.readVarInt(data);
        byte[] reasonBytes = new byte[reasonLength];
        data.readFully(reasonBytes);

        String reason = NBT.extractChatText(reasonBytes);

        connection.getLogger().accept("╔════════════════════════════════════════╗");
        connection.getLogger().accept("║   DISCONNECTED FROM SERVER");
        connection.getLogger().accept("║   Reason: " + reason);
        connection.getLogger().accept("╚════════════════════════════════════════╝");
        connection.disconnect();
    }
}
