package com.riege.rmc.minecraft.protocol.packets.play;

import com.riege.rmc.minecraft.protocol.packets.MinecraftPacket;
import com.riege.rmc.minecraft.protocol.Packet;
import com.riege.rmc.minecraft.protocol.PacketBuffer;

import java.io.IOException;

public class KeepAlivePlayPacket implements Packet {
    private final long keepAliveId;

    public KeepAlivePlayPacket(long keepAliveId) {
        this.keepAliveId = keepAliveId;
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        buffer.writeLong(keepAliveId);
    }

    @Override
    public int getPacketId() {
        return getPacketType().getPacketId();
    }

    @Override
    public MinecraftPacket getPacketType() {
        return MinecraftPacket.KEEP_ALIVE_PLAY_S;
    }
}
