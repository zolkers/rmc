package com.riege.rmc.minecraft.protocol.handler.configuration;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;

import java.io.IOException;

/**
 * Handles Registry Data packet from server.
 * Server packet ID: 0x07 in CONFIGURATION state
 *
 * This packet contains all registry data (dimensions, biomes, etc.)
 * We just consume it without processing since we don't need the data for basic connection.
 */
public class RegistryDataHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        connection.getLogger().accept("Received registry data (" + packet.payload().length + " bytes)");
        // Just consume the packet - registry data is not needed for basic connection
    }
}
