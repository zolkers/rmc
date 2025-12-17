package com.riege.rmc.minecraft.protocol.packets.configuration;

import com.riege.rmc.minecraft.protocol.Packet;
import com.riege.rmc.minecraft.protocol.PacketBuffer;
import com.riege.rmc.minecraft.protocol.packets.MinecraftPacket;

import java.io.IOException;

/**
 * Response to server's known packs request.
 * Packet ID: 0x07 in CONFIGURATION state (Client to Server)
 */
public final class KnownPacksPacket implements Packet {

    @Override
    public MinecraftPacket getPacketType() {
        return MinecraftPacket.SELECT_KNOWN_PACKS_CONFIG_S;
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        // Send empty list - we don't have any known packs
        buffer.writeVarInt(0); // Array length = 0
    }
}
