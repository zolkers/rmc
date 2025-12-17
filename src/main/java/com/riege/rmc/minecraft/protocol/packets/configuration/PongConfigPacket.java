package com.riege.rmc.minecraft.protocol.packets.configuration;

import com.riege.rmc.minecraft.protocol.Packet;
import com.riege.rmc.minecraft.protocol.PacketBuffer;
import com.riege.rmc.minecraft.protocol.packets.MinecraftPacket;

import java.io.IOException;

/**
 * Response to server's ping request.
 * Packet ID: 0x05 in CONFIGURATION state (Client to Server)
 */
public final class PongConfigPacket implements Packet {

    private final int id;

    public PongConfigPacket(int id) {
        this.id = id;
    }

    @Override
    public MinecraftPacket getPacketType() {
        return MinecraftPacket.PONG_CONFIG_S;
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        buffer.writeInt(id);
    }
}
