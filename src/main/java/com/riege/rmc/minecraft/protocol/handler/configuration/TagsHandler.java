package com.riege.rmc.minecraft.protocol.handler.configuration;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;

import java.io.IOException;

/**
 * Handles Tags packet from server.
 * Server packet ID: 0x0D in CONFIGURATION state
 *
 * This packet contains all tags (block tags, item tags, etc.)
 * We just consume it without processing since we don't need the data for basic connection.
 */
public class TagsHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        connection.getLogger().accept("Received tags (" + packet.payload().length + " bytes)");
        // Just consume the packet - tags are not needed for basic connection
    }
}
