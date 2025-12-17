package com.riege.rmc.minecraft.protocol.packets.play;

import com.riege.rmc.minecraft.protocol.Packet;
import com.riege.rmc.minecraft.protocol.PacketBuffer;
import com.riege.rmc.minecraft.protocol.packets.MinecraftPacket;

import java.io.IOException;

public final class ChatMessagePacket implements Packet {
    private final String message;

    public ChatMessagePacket(String message) {
        this.message = message;
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        buffer.writeString(message);
        buffer.writeLong(System.currentTimeMillis());
        buffer.writeLong(0L);
        buffer.writeBoolean(false);

        buffer.writeVarInt(0);

        for (int i = 0; i < 20; i++) {
            buffer.writeByte(0);
        }
    }

    @Override
    public MinecraftPacket getPacketType() {
        return MinecraftPacket.CHAT_MESSAGE_S;
    }
}
