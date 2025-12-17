package com.riege.rmc.minecraft.protocol;

import com.riege.rmc.minecraft.microsoft.AuthenticatedProfile;
import com.riege.rmc.minecraft.protocol.handler.configuration.*;
import com.riege.rmc.minecraft.protocol.handler.play.*;
import com.riege.rmc.minecraft.protocol.packets.MinecraftPacket;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;
import com.riege.rmc.minecraft.protocol.handler.PacketHandlerRegistry;
import com.riege.rmc.minecraft.protocol.logging.PacketLogger;
import com.riege.rmc.minecraft.protocol.packets.handshaking.HandshakePacket;
import com.riege.rmc.minecraft.protocol.packets.login.EncryptionResponsePacket;
import com.riege.rmc.minecraft.protocol.packets.login.LoginAcknowledgedPacket;
import com.riege.rmc.minecraft.protocol.packets.login.LoginStartPacket;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.function.Consumer;

public final class ServerConnection {
    private final String host;
    private final int port;
    private MinecraftConnection connection;
    private final Consumer<String> logger;
    private ConnectionState currentState = ConnectionState.HANDSHAKING;
    private final PacketHandlerRegistry handlerRegistry;
    private final PacketLogger packetLogger;
    private KeepAliveManager keepAliveManager;
    private volatile ConnectionLoop connectionLoop;
    private String transferHost;
    private int transferPort;

    public ServerConnection(String address, Consumer<String> logger) {
        this(address, logger, 0);
    }

    public ServerConnection(String address, Consumer<String> logger, int verbosity) {
        this.logger = logger;
        String[] parts = parseAddress(address);
        this.host = parts[0];
        this.port = Integer.parseInt(parts[1]);

        PacketHandler defaultHandler = (packet, conn) -> {
            var packetInfo = MinecraftPacket.findByIdAndStateAndDirection(
                packet.packetId(),
                MinecraftPacket.State.valueOf(currentState.name()),
                MinecraftPacket.Direction.TO_CLIENT
            );

            if (verbosity > 0) {
                if (packetInfo.isPresent()) {
                    logger.accept(String.format("Unhandled packet 0x%02X (%s) in state %s",
                        packet.packetId(), packetInfo.get().getName(), currentState));
                } else {
                    logger.accept(String.format("Unknown packet 0x%02X in state %s",
                        packet.packetId(), currentState));
                }
            }
        };
        this.handlerRegistry = new PacketHandlerRegistry(defaultHandler);

        if (verbosity > 0) {
            this.packetLogger = new PacketLogger(verbosity, logger);
        } else {
            this.packetLogger = null;
        }

        registerHandlers();
    }

    private void registerHandlers() {
        handlerRegistry.register(MinecraftPacket.CUSTOM_PAYLOAD_CONFIG_C,
            new CustomPayloadHandler());
        handlerRegistry.register(MinecraftPacket.DISCONNECT_CONFIG_C,
            new DisconnectConfigHandler());
        handlerRegistry.register(MinecraftPacket.FINISH_CONFIGURATION_C,
            new FinishConfigurationHandler());
        handlerRegistry.register(MinecraftPacket.KEEP_ALIVE_CONFIG_C,
            new KeepAliveConfigHandler());
        handlerRegistry.register(MinecraftPacket.PING_CONFIG_C,
            new PingConfigHandler());
        handlerRegistry.register(MinecraftPacket.REGISTRY_DATA_C,
            new RegistryDataHandler());
        handlerRegistry.register(MinecraftPacket.TRANSFER_CONFIG_C,
            new TransferHandler());
        handlerRegistry.register(MinecraftPacket.FEATURE_FLAGS_C,
            new FeatureFlagsHandler());
        handlerRegistry.register(MinecraftPacket.TAGS_CONFIG_C,
            new TagsHandler());
        handlerRegistry.register(MinecraftPacket.SELECT_KNOWN_PACKS_CONFIG_C,
            new KnownPacksHandler());

        handlerRegistry.register(MinecraftPacket.KICK_DISCONNECT_C,
            new DisconnectPlayHandler());
        handlerRegistry.register(MinecraftPacket.KEEP_ALIVE_PLAY_C,
            new KeepAlivePlayHandler());
        handlerRegistry.register(MinecraftPacket.LOGIN_PLAY_C,
            new LoginPlayHandler());
        handlerRegistry.register(MinecraftPacket.PLAYER_CHAT_C,
            new PlayerChatHandler());
        handlerRegistry.register(MinecraftPacket.SYSTEM_CHAT_C,
            new SystemChatHandler());
        handlerRegistry.register(MinecraftPacket.TRANSFER_PLAY_C,
            new TransferPlayHandler());
    }

    public void connect(AuthenticatedProfile profile) throws Exception {
        logger.accept("Connecting to " + host + ":" + port + "...");

        connection = new MinecraftConnection(host, port);
        if (packetLogger != null) {
            connection.setPacketLogger(packetLogger);
        }
        logger.accept("TCP connection established");

        currentState = ConnectionState.HANDSHAKING;
        connection.setCurrentState(currentState);

        HandshakePacket handshake = new HandshakePacket(host, port, HandshakePacket.NextState.LOGIN);
        connection.sendPacket(handshake);
        logger.accept("Handshake sent");

        currentState = ConnectionState.LOGIN;
        connection.setCurrentState(currentState);

        LoginStartPacket loginStart = new LoginStartPacket(profile.username(), profile.uuid());
        connection.sendPacket(loginStart);
        logger.accept("Login request sent");

        MinecraftConnection.PacketData response = connection.readPacket();

        switch (response.packetId()) {
            case 0x01 -> handleEncryptionRequest(response, profile);
            case 0x02 -> handleLoginSuccess();
            case 0x00 -> handleDisconnect(response);
            default -> throw new IOException("Unknown packet ID: 0x" + Integer.toHexString(response.packetId()));
        }
    }

    private void handleEncryptionRequest(MinecraftConnection.PacketData packet, AuthenticatedProfile profile) throws Exception {
        logger.accept("Encryption requested, authenticating with Mojang...");

        DataInputStream data = packet.getDataStream();

        int serverIdLength = VarInt.readVarInt(data);
        byte[] serverIdBytes = new byte[serverIdLength];
        data.readFully(serverIdBytes);
        String serverId = new String(serverIdBytes);

        int publicKeyLength = VarInt.readVarInt(data);
        byte[] publicKey = new byte[publicKeyLength];
        data.readFully(publicKey);

        int verifyTokenLength = VarInt.readVarInt(data);
        byte[] verifyToken = new byte[verifyTokenLength];
        data.readFully(verifyToken);

        byte[] sharedSecret = EncryptionUtils.generateSharedSecret();

        byte[] encryptedSecret = EncryptionUtils.encryptRSA(publicKey, sharedSecret);
        byte[] encryptedToken = EncryptionUtils.encryptRSA(publicKey, verifyToken);

        String serverHash = EncryptionUtils.generateServerIdHash(serverId, sharedSecret, publicKey);
        SessionServerAuth.joinServer(profile.accessToken(), profile.uuid().toString(), serverHash);
        logger.accept("Authenticated with session server");

        sendEncryptionResponse(encryptedSecret, encryptedToken);

        connection.enableEncryption(sharedSecret);
        logger.accept("Encryption enabled");

        MinecraftConnection.PacketData successPacket = connection.readPacket();
        if (successPacket.packetId() == 0x02) {
            handleLoginSuccess();
        } else if (successPacket.packetId() == 0x03) {
            handleSetCompression(successPacket);
            successPacket = connection.readPacket();
            if (successPacket.packetId() == 0x02) {
                handleLoginSuccess();
            } else {
                throw new IOException("Expected LOGIN_SUCCESS (0x02) after compression, got: 0x" +
                    Integer.toHexString(successPacket.packetId()));
            }
        } else {
            throw new IOException("Expected LOGIN_SUCCESS (0x02) or SET_COMPRESSION (0x03) after encryption, got: 0x" +
                Integer.toHexString(successPacket.packetId()));
        }
    }

    private void sendEncryptionResponse(byte[] encryptedSecret, byte[] encryptedToken) throws IOException {
        EncryptionResponsePacket encryptionResponse = new EncryptionResponsePacket(encryptedSecret, encryptedToken);
        connection.sendPacket(encryptionResponse);
    }

    private void handleSetCompression(MinecraftConnection.PacketData packet) throws IOException {
        DataInputStream data = packet.getDataStream();
        int threshold = VarInt.readVarInt(data);
        connection.enableCompression(threshold);
        logger.accept("Compression enabled with threshold: " + threshold);
    }

    private void handleLoginSuccess() throws IOException {
        logger.accept("Login successful!");
        logger.accept("=== SENDING LOGIN_ACKNOWLEDGED ===");
        logger.accept("Current state: " + currentState);
        logger.accept("Socket connected: " + connection.isConnected());
        logger.accept("Socket closed: " + connection.getSocket().isClosed());
        logger.accept("Socket input shutdown: " + connection.getSocket().isInputShutdown());
        logger.accept("Socket output shutdown: " + connection.getSocket().isOutputShutdown());

        LoginAcknowledgedPacket loginAck = new LoginAcknowledgedPacket();
        logger.accept("Sending LOGIN_ACKNOWLEDGED packet (0x03) with no data...");
        connection.sendPacket(loginAck);
        logger.accept("Sent login acknowledgement");

        logger.accept("=== AFTER SENDING LOGIN_ACKNOWLEDGED ===");
        logger.accept("Socket connected: " + connection.isConnected());
        logger.accept("Socket closed: " + connection.getSocket().isClosed());
        logger.accept("Socket input shutdown: " + connection.getSocket().isInputShutdown());
        logger.accept("Socket output shutdown: " + connection.getSocket().isOutputShutdown());

        logger.accept("=== TRANSITIONING TO CONFIGURATION STATE ===");
        logger.accept("Old state: " + currentState);
        currentState = ConnectionState.CONFIGURATION;
        connection.setCurrentState(currentState);
        logger.accept("New state: " + currentState);
        logger.accept("Connection state updated: " + connection.getCurrentState());

        keepAliveManager = new KeepAliveManager(30);

        logger.accept("Waiting for server configuration packets...");

        processConfigurationPhase();
    }

    private void processConfigurationPhase() throws IOException {
        logger.accept("Processing configuration phase...");
        int packetCount = 0;
        while (currentState == ConnectionState.CONFIGURATION && connection.isConnected()) {
            try {
                logger.accept("Waiting for configuration packet #" + (++packetCount) + "...");
                MinecraftConnection.PacketData packet = connection.readPacket();
                logger.accept("Received packet 0x" + Integer.toHexString(packet.packetId()) +
                             " (" + packet.payload().length + " bytes)");

                PacketHandler handler = handlerRegistry.getHandler(
                    currentState, MinecraftPacket.Direction.TO_CLIENT, packet.packetId()
                );

                logger.accept("Handling packet with " + handler.getClass().getSimpleName());
                handler.handle(packet, this);
                logger.accept("Packet handled successfully");

            } catch (IOException e) {
                logger.accept("Error reading packet: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                throw e;
            }
        }
        logger.accept("Configuration phase completed, state is now: " + currentState);
    }

    public void transitionToPlay() {
        currentState = ConnectionState.PLAY;
        connection.setCurrentState(currentState);
        logger.accept("→ PLAY state");
        logger.accept("Connected to server!");

        connectionLoop = new ConnectionLoop(connection, handlerRegistry, currentState, logger, this);
        Thread loopThread = new Thread(connectionLoop, "connection-loop");
        loopThread.setDaemon(true);
        loopThread.start();
    }

    private void handleDisconnect(MinecraftConnection.PacketData packet) throws IOException {
        DataInputStream data = packet.getDataStream();
        int reasonLength = VarInt.readVarInt(data);
        byte[] reasonBytes = new byte[reasonLength];
        data.readFully(reasonBytes);
        String reason = new String(reasonBytes);
        throw new IOException("Server disconnected: " + reason);
    }

    public void disconnect() {
        if (connectionLoop != null) {
            connectionLoop.stop();
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private String[] parseAddress(String address) {
        if (address.contains(":")) {
            return address.split(":", 2);
        }
        return new String[]{address, "25565"};
    }

    public MinecraftConnection getConnection() {
        return connection;
    }

    public Consumer<String> getLogger() {
        return logger;
    }

    public KeepAliveManager getKeepAliveManager() {
        return keepAliveManager;
    }

    public void setTransferTarget(String host, int port) {
        this.transferHost = host;
        this.transferPort = port;
    }

    public String getTransferHost() {
        return transferHost;
    }

    public int getTransferPort() {
        return transferPort;
    }

    public boolean hasTransferTarget() {
        return transferHost != null;
    }
}
