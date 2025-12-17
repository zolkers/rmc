package com.riege.rmc.minecraft.protocol.handler.play;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.packets.utils.TransferUtils;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;

import java.io.IOException;

/**
 * Handles Transfer packet from server during gameplay (server redirection).
 * Server packet ID: 0x7A in PLAY state
 * <p>
 * This packet tells the client to disconnect and connect to a different server.
 * </p>
 */
public class TransferPlayHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        TransferUtils.handleTransfer(packet, connection);
    }
}
