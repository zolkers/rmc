package com.riege.rmc.minecraft.protocol.logging;

import com.riege.rmc.minecraft.protocol.packets.MinecraftPacket;
import com.riege.rmc.minecraft.protocol.ConnectionState;
import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.Packet;

import java.util.function.Consumer;

public class PacketLogger {
    private final int verbosityLevel;
    private final Consumer<String> output;

    public PacketLogger(int verbosityLevel, Consumer<String> output) {
        this.verbosityLevel = verbosityLevel;
        this.output = output;
    }

    public void logIncoming(MinecraftConnection.PacketData packet, ConnectionState state) {
        if (verbosityLevel == 0) return;

        String message = formatIncomingPacket(packet.packetId(), state,
                                             packet.payload().length, packet.payload());
        output.accept(message);
    }

    public void logOutgoing(Packet packet, ConnectionState state) {
        if (verbosityLevel == 0) return;

        int packetId = packet.getPacketType().getPacketId();
        String message = formatOutgoingPacket(packetId, state);
        output.accept(message);
    }

    private String formatIncomingPacket(int packetId, ConnectionState state, int size, byte[] payload) {
        MinecraftPacket.State mcState = mapConnectionStateToPacketState(state);
        var packetInfoOpt = MinecraftPacket.findByIdAndStateAndDirection(
            packetId, mcState, MinecraftPacket.Direction.TO_CLIENT
        );

        String hexId = String.format("0x%02X", packetId);
        String packetName = packetInfoOpt.map(MinecraftPacket::getName).orElse("unknown");

        if (verbosityLevel == 1) {
            return String.format("[RECV] %s %s", hexId, packetName);
        } else if (verbosityLevel == 2) {
            String direction = packetInfoOpt.map(p -> p.getDirection().name()).orElse("TO_CLIENT");
            return String.format("[RECV] %s %s %s (%d bytes) [%s]",
                hexId, packetName, direction, size, state.name());
        } else {
            String direction = packetInfoOpt.map(p -> p.getDirection().name()).orElse("TO_CLIENT");
            String hexDump = createHexDump(payload, 256);
            return String.format("[RECV] %s %s %s (%d bytes) [%s]\n%s",
                hexId, packetName, direction, size, state.name(), hexDump);
        }
    }

    private String formatOutgoingPacket(int packetId, ConnectionState state) {
        MinecraftPacket.State mcState = mapConnectionStateToPacketState(state);
        var packetInfoOpt = MinecraftPacket.findByIdAndStateAndDirection(
            packetId, mcState, MinecraftPacket.Direction.TO_SERVER
        );

        String hexId = String.format("0x%02X", packetId);
        String packetName = packetInfoOpt.map(MinecraftPacket::getName).orElse("unknown");

        if (verbosityLevel == 1) {
            return String.format("[SEND] %s %s", hexId, packetName);
        } else {
            String direction = packetInfoOpt.map(p -> p.getDirection().name()).orElse("TO_SERVER");
            return String.format("[SEND] %s %s %s [%s]",
                hexId, packetName, direction, state.name());
        }
    }

    private String createHexDump(byte[] data, int maxBytes) {
        StringBuilder sb = new StringBuilder();
        int length = Math.min(data.length, maxBytes);

        for (int i = 0; i < length; i += 16) {
            sb.append(String.format("  %04X: ", i));

            int lineEnd = Math.min(i + 16, length);
            for (int j = i; j < lineEnd; j++) {
                sb.append(String.format("%02X ", data[j]));
            }

            for (int j = lineEnd; j < i + 16; j++) {
                sb.append("   ");
            }

            sb.append(" | ");
            for (int j = i; j < lineEnd; j++) {
                char c = (char) data[j];
                sb.append(c >= 32 && c <= 126 ? c : '.');
            }
            sb.append("\n");
        }

        if (data.length > maxBytes) {
            sb.append(String.format("  ... (%d more bytes)\n", data.length - maxBytes));
        }

        return sb.toString();
    }

    private MinecraftPacket.State mapConnectionStateToPacketState(ConnectionState state) {
        return switch (state) {
            case HANDSHAKING -> MinecraftPacket.State.HANDSHAKING;
            case LOGIN -> MinecraftPacket.State.LOGIN;
            case CONFIGURATION -> MinecraftPacket.State.CONFIGURATION;
            case PLAY -> MinecraftPacket.State.PLAY;
        };
    }
}
