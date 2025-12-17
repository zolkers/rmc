package com.riege.rmc.minecraft.protocol.handler.configuration;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;
import com.riege.rmc.minecraft.protocol.packets.configuration.FinishConfigurationPacket;

import java.io.IOException;

public final class FinishConfigurationHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        connection.getLogger().accept("Configuration complete, acknowledging...");

        FinishConfigurationPacket ack = new FinishConfigurationPacket();
        connection.getConnection().sendPacket(ack);

        connection.transitionToPlay();
    }
}
