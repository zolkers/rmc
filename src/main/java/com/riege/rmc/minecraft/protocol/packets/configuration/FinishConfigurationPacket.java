package com.riege.rmc.minecraft.protocol.packets.configuration;

import com.riege.rmc.minecraft.protocol.packets.MinecraftPacket;
import com.riege.rmc.minecraft.protocol.Packet;
import com.riege.rmc.minecraft.protocol.PacketBuffer;

import java.io.IOException;

public final class FinishConfigurationPacket implements Packet {

    public FinishConfigurationPacket() {
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        // No payload - empty packet
    }

    @Override
    public int getPacketId() {
        return getPacketType().getPacketId();
    }

    @Override
    public MinecraftPacket getPacketType() {
        return MinecraftPacket.FINISH_CONFIGURATION_S;
    }
}
