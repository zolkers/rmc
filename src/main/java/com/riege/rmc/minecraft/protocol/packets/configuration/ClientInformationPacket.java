package com.riege.rmc.minecraft.protocol.packets.configuration;

import com.riege.rmc.minecraft.protocol.Packet;
import com.riege.rmc.minecraft.protocol.PacketBuffer;
import com.riege.rmc.minecraft.protocol.packets.MinecraftPacket;

import java.io.IOException;

/**
 * Sent by client to inform server about client settings.
 * Packet ID: 0x00 in CONFIGURATION state (Client to Server)
 */
public final class ClientInformationPacket implements Packet {

    private final String locale;
    private final byte viewDistance;
    private final int chatMode;
    private final boolean chatColors;
    private final byte displayedSkinParts;
    private final int mainHand;
    private final boolean textFilteringEnabled;
    private final boolean allowServerListings;

    public ClientInformationPacket() {
        this.locale = "en_US";
        this.viewDistance = 10;
        this.chatMode = 0;
        this.chatColors = true;
        this.displayedSkinParts = (byte) 0x7F;
        this.mainHand = 1;
        this.textFilteringEnabled = false;
        this.allowServerListings = true;
    }

    @Override
    public MinecraftPacket getPacketType() {
        return MinecraftPacket.SETTINGS_CONFIG_S;
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        buffer.writeString(locale);
        buffer.writeBytes(new byte[]{viewDistance});
        buffer.writeVarInt(chatMode);
        buffer.writeBoolean(chatColors);
        buffer.writeBytes(new byte[]{displayedSkinParts});
        buffer.writeVarInt(mainHand);
        buffer.writeBoolean(textFilteringEnabled);
        buffer.writeBoolean(allowServerListings);
    }
}
