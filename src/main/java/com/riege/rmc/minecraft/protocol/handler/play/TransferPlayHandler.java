package com.riege.rmc.minecraft.protocol.handler.play;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.VarInt;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Handles Transfer packet from server during gameplay (server redirection).
 * Server packet ID: 0x7A in PLAY state
 *
 * This packet tells the client to disconnect and connect to a different server.
 */
public class TransferPlayHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        DataInputStream data = packet.getDataStream();

        // Read host
        int hostLength = VarInt.readVarInt(data);
        byte[] hostBytes = new byte[hostLength];
        data.readFully(hostBytes);
        String host = new String(hostBytes);

        // Read port
        int port = data.readInt();

        connection.getLogger().accept("Server requested transfer to " + host + ":" + port);

        // Store the transfer information in the connection
        connection.setTransferTarget(host, port);

        // Disconnect - the API layer will handle reconnection
        connection.disconnect();
    }
}
