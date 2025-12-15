package com.riege.rmc.minecraft.protocol;

import com.riege.rmc.minecraft.microsoft.AuthenticatedProfile;
import com.riege.rmc.minecraft.protocol.packets.handshaking.HandshakePacket;
import com.riege.rmc.minecraft.protocol.packets.login.EncryptionResponsePacket;
import com.riege.rmc.minecraft.protocol.packets.login.LoginStartPacket;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.function.Consumer;

public final class ServerConnection {
    private final String host;
    private final int port;
    private MinecraftConnection connection;
    private final Consumer<String> logger;

    public ServerConnection(String address, Consumer<String> logger) {
        this.logger = logger;
        String[] parts = parseAddress(address);
        this.host = parts[0];
        this.port = Integer.parseInt(parts[1]);
    }

    public void connect(AuthenticatedProfile profile) throws Exception {
        logger.accept("Connecting to " + host + ":" + port + "...");

        connection = new MinecraftConnection(host, port);
        logger.accept("TCP connection established");

        HandshakePacket handshake = new HandshakePacket(host, port, HandshakePacket.NextState.LOGIN);
        connection.sendPacket(handshake);
        logger.accept("Handshake sent");

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
        logger.accept("Connected to server!");
        // TODO: Transition to play state
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
}
