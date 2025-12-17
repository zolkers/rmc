package com.riege.rmc.minecraft.protocol.handler.configuration;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;

import java.io.IOException;

/**
 * Handles Feature Flags packet from server.
 * Server packet ID: 0x0C in CONFIGURATION state
 *
 * This packet contains feature flags for experimental features.
 * We just consume it without processing.
 */
public class FeatureFlagsHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        connection.getLogger().accept("Received feature flags");
        // Just consume the packet - feature flags are not needed for basic connection
    }
}
