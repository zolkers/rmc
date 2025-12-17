package com.riege.rmc.minecraft.protocol.handler;

import com.riege.rmc.minecraft.protocol.packets.MinecraftPacket;
import com.riege.rmc.minecraft.protocol.ConnectionState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PacketHandlerRegistry {
    private final Map<String, PacketHandler> handlers = new ConcurrentHashMap<>();
    private final PacketHandler defaultHandler;

    public PacketHandlerRegistry(PacketHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    public void register(ConnectionState state, MinecraftPacket.Direction direction,
                        int packetId, PacketHandler handler) {
        String key = makeKey(state, direction, packetId);
        handlers.put(key, handler);
    }

    public PacketHandler getHandler(ConnectionState state, MinecraftPacket.Direction direction,
                                    int packetId) {
        String key = makeKey(state, direction, packetId);
        return handlers.getOrDefault(key, defaultHandler);
    }

    private String makeKey(ConnectionState state, MinecraftPacket.Direction direction, int packetId) {
        return state.name() + "_" + direction.name() + "_" + packetId;
    }
}
