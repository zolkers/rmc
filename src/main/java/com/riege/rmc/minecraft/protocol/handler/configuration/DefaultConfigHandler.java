package com.riege.rmc.minecraft.protocol.handler.configuration;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;

import java.io.IOException;

public class DefaultConfigHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        // Silently accept unknown configuration packets
        // Most configuration packets can be safely ignored in a minimal client
    }
}
