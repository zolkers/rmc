package com.riege.rmc.minecraft.protocol;

import com.riege.rmc.minecraft.protocol.packets.MinecraftPacket;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;
import com.riege.rmc.minecraft.protocol.handler.PacketHandlerRegistry;

import java.io.IOException;
import java.util.function.Consumer;

public class ConnectionLoop implements Runnable {
    private volatile boolean running = true;
    private final MinecraftConnection connection;
    private final PacketHandlerRegistry registry;
    private final ConnectionState state;
    private final Consumer<String> logger;
    private final ServerConnection serverConnection;

    public ConnectionLoop(MinecraftConnection connection, PacketHandlerRegistry registry,
                         ConnectionState state, Consumer<String> logger, ServerConnection serverConnection) {
        this.connection = connection;
        this.registry = registry;
        this.state = state;
        this.logger = logger;
        this.serverConnection = serverConnection;
    }

    @Override
    public void run() {
        logger.accept("Connection loop started");
        while (running && connection.isConnected()) {
            try {
                MinecraftConnection.PacketData packet = connection.readPacket();
                PacketHandler handler = registry.getHandler(
                    state, MinecraftPacket.Direction.TO_CLIENT, packet.packetId()
                );

                handler.handle(packet, serverConnection);

            } catch (IOException e) {
                if (running) {
                    logger.accept("Connection error: " + e.getMessage());
                }
                running = false;
            } catch (Exception e) {
                logger.accept("Handler error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        logger.accept("Connection loop terminated");
    }

    public void stop() {
        running = false;
    }
}
