package com.riege.rmc.minecraft.protocol.handler.configuration;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.packets.utils.TransferUtils;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;

import java.io.IOException;

/**
 * Handles Transfer packet from server (server redirection).
 * Server packet ID: 0x0B in CONFIGURATION state
 * <p>
 * This packet tells the client to disconnect and connect to a different server.
 * </p>
 */
public class TransferHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        TransferUtils.handleTransfer(packet, connection);
    }
}
