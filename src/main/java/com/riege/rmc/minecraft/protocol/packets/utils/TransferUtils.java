package com.riege.rmc.minecraft.protocol.packets.utils;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.VarInt;

import java.io.DataInputStream;
import java.io.IOException;

public final class TransferUtils {

    private TransferUtils() {}

    public static void handleTransfer(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        DataInputStream data = packet.getDataStream();

        int hostLength = VarInt.readVarInt(data);
        byte[] hostBytes = new byte[hostLength];
        data.readFully(hostBytes);
        String host = new String(hostBytes);

        int port = data.readInt();

        connection.getLogger().accept("Server requested transfer to " + host + ":" + port);

        connection.setTransferTarget(host, port);
        connection.disconnect();
    }
}
