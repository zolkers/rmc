package com.riege.rmc.minecraft.protocol;

import com.riege.rmc.minecraft.microsoft.AuthenticatedProfile;
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

    public ServerConnection(String address, Consumer<String> logger) {
        this(address, logger, 0);
    }

    public ServerConnection(String address, Consumer<String> logger, int verbosity) {
        this.logger = logger;
        String[] parts = parseAddress(address);
        this.host = parts[0];
        this.port = Integer.parseInt(parts[1]);

        PacketHandler defaultHandler = (packet, conn) -> {
            logger.accept(String.format("Unknown packet 0x%02X in state %s", packet.packetId(), currentState));
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
        // Configuration state handlers
        handlerRegistry.register(ConnectionState.CONFIGURATION, MinecraftPacket.Direction.TO_CLIENT,
            0x04, new com.riege.rmc.minecraft.protocol.handler.configuration.KeepAliveConfigHandler());
        handlerRegistry.register(ConnectionState.CONFIGURATION, MinecraftPacket.Direction.TO_CLIENT,
            0x03, new com.riege.rmc.minecraft.protocol.handler.configuration.FinishConfigurationHandler());

        // Play state handlers
        handlerRegistry.register(ConnectionState.PLAY, MinecraftPacket.Direction.TO_CLIENT,
            0x27, new com.riege.rmc.minecraft.protocol.handler.play.KeepAlivePlayHandler());
        handlerRegistry.register(ConnectionState.PLAY, MinecraftPacket.Direction.TO_CLIENT,
            0x2C, new com.riege.rmc.minecraft.protocol.handler.play.LoginPlayHandler());
        handlerRegistry.register(ConnectionState.PLAY, MinecraftPacket.Direction.TO_CLIENT,
            0x1D, new com.riege.rmc.minecraft.protocol.handler.play.DisconnectPlayHandler());
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
            case 0x02 -> handleLoginSuccess(response);
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
            handleLoginSuccess(successPacket);
        } else if (successPacket.packetId() == 0x03) {
            // Set compression
            handleSetCompression(successPacket);
            successPacket = connection.readPacket();
            if (successPacket.packetId() == 0x02) {
                handleLoginSuccess(successPacket);
            }
        }
    }

    private void sendEncryptionResponse(byte[] encryptedSecret, byte[] encryptedToken) throws IOException {
        EncryptionResponsePacket encryptionResponse = new EncryptionResponsePacket(encryptedSecret, encryptedToken);
        connection.sendPacket(encryptionResponse);
    }

    private void handleSetCompression(MinecraftConnection.PacketData packet) throws IOException {
        DataInputStream data = packet.getDataStream();
        int threshold = VarInt.readVarInt(data);
        logger.accept("Compression enabled with threshold: " + threshold);
        // TODO: Implement compression support
    }

    private void handleLoginSuccess(MinecraftConnection.PacketData packet) throws IOException {
        logger.accept("Login successful!");

        currentState = ConnectionState.CONFIGURATION;
        connection.setCurrentState(currentState);

        LoginAcknowledgedPacket loginAck = new LoginAcknowledgedPacket();
        connection.sendPacket(loginAck);
        logger.accept("→ CONFIGURATION state");

        keepAliveManager = new KeepAliveManager(30);

        processConfigurationPhase();
    }

    private void processConfigurationPhase() throws IOException {
        logger.accept("Processing configuration phase...");
        while (currentState == ConnectionState.CONFIGURATION && connection.isConnected()) {
            MinecraftConnection.PacketData packet = connection.readPacket();
            PacketHandler handler = handlerRegistry.getHandler(
                currentState, MinecraftPacket.Direction.TO_CLIENT, packet.packetId()
            );
            handler.handle(packet, this);
        }
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

    // Getters for packet handlers to access components
    public MinecraftConnection getConnection() {
        return connection;
    }

    public Consumer<String> getLogger() {
        return logger;
    }

    public ConnectionState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(ConnectionState state) {
        this.currentState = state;
        connection.setCurrentState(state);
    }

    public KeepAliveManager getKeepAliveManager() {
        return keepAliveManager;
    }

    public PacketHandlerRegistry getHandlerRegistry() {
        return handlerRegistry;
    }
}
