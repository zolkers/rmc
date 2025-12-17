package com.riege.rmc.minecraft.protocol.packets.handshaking;

import com.riege.rmc.minecraft.protocol.packets.MinecraftPacket;
import com.riege.rmc.minecraft.protocol.Packet;
import com.riege.rmc.minecraft.protocol.PacketBuffer;

import java.io.IOException;

public record HandshakePacket(String serverAddress, int serverPort, NextState nextState) implements Packet {
    private static final int PROTOCOL_VERSION = 769; // Minecraft 1.21.4

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        buffer.writeVarInt(PROTOCOL_VERSION);
        buffer.writeString(serverAddress);
        buffer.writeShort(serverPort);
        buffer.writeVarInt(nextState.id);
    }

    @Override
    public MinecraftPacket getPacketType() {
        return MinecraftPacket.SET_PROTOCOL_S;
    }

    public enum NextState {
        STATUS(1),
        LOGIN(2),
        TRANSFER(3);

        private final int id;

        NextState(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
