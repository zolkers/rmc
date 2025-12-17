package com.riege.rmc.minecraft.protocol.handler.configuration;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;
import com.riege.rmc.minecraft.protocol.packets.configuration.KnownPacksPacket;

import java.io.IOException;

/**
 * Handles Known Packs request from server.
 * Server packet ID: 0x0E in CONFIGURATION state
 */
public class KnownPacksHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        connection.getLogger().accept("Server requesting known packs...");

        // Respond with empty list (we don't have any packs)
        KnownPacksPacket response = new KnownPacksPacket();
        connection.getConnection().sendPacket(response);

        connection.getLogger().accept("Sent known packs response");
    }
}
