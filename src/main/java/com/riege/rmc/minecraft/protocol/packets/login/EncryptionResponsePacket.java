package com.riege.rmc.minecraft.protocol.packets.login;

import com.riege.rmc.minecraft.protocol.packets.MinecraftPacket;
import com.riege.rmc.minecraft.protocol.Packet;
import com.riege.rmc.minecraft.protocol.PacketBuffer;

import java.io.IOException;

public record EncryptionResponsePacket(byte[] encryptedSharedSecret, byte[] encryptedVerifyToken) implements Packet {

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        buffer.writeVarInt(encryptedSharedSecret.length);
        buffer.writeBytes(encryptedSharedSecret);
        buffer.writeVarInt(encryptedVerifyToken.length);
        buffer.writeBytes(encryptedVerifyToken);
    }

    @Override
    public MinecraftPacket getPacketType() {
        return MinecraftPacket.ENCRYPTION_BEGIN_LOGIN_S;
    }
}
