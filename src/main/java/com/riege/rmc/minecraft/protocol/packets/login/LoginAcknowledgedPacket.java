package com.riege.rmc.minecraft.protocol.packets.login;

import com.riege.rmc.minecraft.packet.MinecraftPacket;
import com.riege.rmc.minecraft.protocol.Packet;
import com.riege.rmc.minecraft.protocol.PacketBuffer;

import java.io.IOException;

public class LoginAcknowledgedPacket implements Packet {

    @Override
    public void write(PacketBuffer buffer) {
        // No data for this packet
    }

    @Override
    public MinecraftPacket getPacketType() {
        return MinecraftPacket.LOGIN_ACKNOWLEDGED_S;
    }
}
