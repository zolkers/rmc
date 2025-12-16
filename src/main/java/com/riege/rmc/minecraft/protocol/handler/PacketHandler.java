package com.riege.rmc.minecraft.protocol.handler;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;

import java.io.IOException;

@FunctionalInterface
public interface PacketHandler {
    void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException;
}
