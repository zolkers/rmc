package com.riege.rmc.minecraft.protocol.packets.login;

import com.riege.rmc.minecraft.packet.MinecraftPacket;
import com.riege.rmc.minecraft.protocol.Packet;
import com.riege.rmc.minecraft.protocol.PacketBuffer;

import java.io.IOException;
import java.util.UUID;

public class LoginStartPacket implements Packet {
    private final String username;
    private final UUID uuid;

    public LoginStartPacket(String username, UUID uuid) {
        this.username = username;
        this.uuid = uuid;
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        buffer.writeString(username);
        buffer.writeUUID(uuid);
    }

    @Override
    public MinecraftPacket getPacketType() {
        return MinecraftPacket.LOGIN_START_S;
    }
}
