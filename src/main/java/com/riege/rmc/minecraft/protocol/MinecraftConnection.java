package com.riege.rmc.minecraft.protocol;

import com.riege.rmc.minecraft.protocol.logging.PacketLogger;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.zip.Inflater;
import java.util.zip.Deflater;

public class MinecraftConnection implements AutoCloseable {
    private final Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private boolean encrypted = false;
    private Cipher encryptCipher;
    private Cipher decryptCipher;
    private PacketLogger packetLogger;
    private ConnectionState currentState = ConnectionState.HANDSHAKING;
    private int compressionThreshold = -1; // -1 means no compression

    public MinecraftConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void setPacketLogger(PacketLogger logger) {
        this.packetLogger = logger;
    }

    public void setCurrentState(ConnectionState state) {
        this.currentState = state;
    }

    public void sendPacket(Packet packet) throws IOException {
        PacketBuffer buffer = new PacketBuffer();

        buffer.writeVarInt(packet.getPacketId());

        packet.write(buffer);

        byte[] packetData = buffer.toByteArray();

        PacketBuffer lengthBuffer = new PacketBuffer();
        lengthBuffer.writeVarInt(packetData.length);

        byte[] lengthBytes = lengthBuffer.toByteArray();

        output.write(lengthBytes);
        output.write(packetData);
        output.flush();

        if (packetLogger != null) {
            packetLogger.logOutgoing(packet, currentState);
        }
    }

    public PacketData readPacket() throws IOException {
        int packetLength = VarInt.readVarInt(input);

        if (packetLength <= 0) {
            throw new IOException("Invalid packet length: " + packetLength);
        }

        byte[] packetData = new byte[packetLength];
        input.readFully(packetData);

        DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(packetData));

        // Handle compression if enabled
        if (compressionThreshold >= 0) {
            int dataLength = VarInt.readVarInt(dataStream);

            if (dataLength == 0) {
                // Not compressed, read normally
                int packetId = VarInt.readVarInt(dataStream);
                int remaining = packetLength - VarInt.getVarIntSize(dataLength) - VarInt.getVarIntSize(packetId);
                byte[] payload = new byte[remaining];
                dataStream.readFully(payload);

                PacketData packet = new PacketData(packetId, payload);
                if (packetLogger != null) {
                    packetLogger.logIncoming(packet, currentState);
                }
                return packet;
            } else {
                // Compressed, decompress first
                int compressedSize = packetLength - VarInt.getVarIntSize(dataLength);
                byte[] compressedData = new byte[compressedSize];
                dataStream.readFully(compressedData);

                byte[] decompressedData = decompress(compressedData, dataLength);
                DataInputStream decompressedStream = new DataInputStream(new ByteArrayInputStream(decompressedData));

                int packetId = VarInt.readVarInt(decompressedStream);
                int remaining = dataLength - VarInt.getVarIntSize(packetId);
                byte[] payload = new byte[remaining];
                decompressedStream.readFully(payload);

                PacketData packet = new PacketData(packetId, payload);
                if (packetLogger != null) {
                    packetLogger.logIncoming(packet, currentState);
                }
                return packet;
            }
        } else {
            // No compression
            int packetId = VarInt.readVarInt(dataStream);
            int remaining = packetLength - VarInt.getVarIntSize(packetId);
            byte[] payload = new byte[remaining];
            dataStream.readFully(payload);

            PacketData packet = new PacketData(packetId, payload);
            if (packetLogger != null) {
                packetLogger.logIncoming(packet, currentState);
            }
            return packet;
        }
    }

    public void enableEncryption(byte[] sharedSecret) throws GeneralSecurityException, IOException {
        SecretKey key = new SecretKeySpec(sharedSecret, "AES");

        encryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(sharedSecret));

        decryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
        decryptCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(sharedSecret));

        // Wrap the existing streams with cipher streams
        this.output = new DataOutputStream(new BufferedOutputStream(new CipherOutputStream(socket.getOutputStream(), encryptCipher)));
        this.input = new DataInputStream(new BufferedInputStream(new CipherInputStream(socket.getInputStream(), decryptCipher)));

        this.encrypted = true;
    }

    public void enableCompression(int threshold) {
        this.compressionThreshold = threshold;
    }

    private byte[] decompress(byte[] data, int uncompressedSize) throws IOException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        byte[] result = new byte[uncompressedSize];
        try {
            int resultLength = inflater.inflate(result);
            if (resultLength != uncompressedSize) {
                throw new IOException("Decompressed size mismatch: expected " + uncompressedSize + ", got " + resultLength);
            }
        } catch (Exception e) {
            throw new IOException("Failed to decompress packet data", e);
        } finally {
            inflater.end();
        }

        return result;
    }

    private byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];

        try {
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
        } finally {
            deflater.end();
        }

        return outputStream.toByteArray();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public record PacketData(int packetId, byte[] payload) {

        public DataInputStream getDataStream() {
                return new DataInputStream(new ByteArrayInputStream(payload));
            }
        }
}
