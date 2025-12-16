package com.riege.rmc.minecraft.protocol;

import com.riege.rmc.minecraft.protocol.packets.MinecraftPacket;

import java.io.IOException;

public interface Packet {
    void write(PacketBuffer buffer) throws IOException;
    MinecraftPacket getPacketType();

    default int getPacketId() {
        return getPacketType().getPacketId();
    }
}
