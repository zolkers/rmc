package com.riege.rmc.minecraft.protocol;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.GeneralSecurityException;

public class MinecraftConnection implements AutoCloseable {
    private final Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private boolean encrypted = false;
    private Cipher encryptCipher;
    private Cipher decryptCipher;

    public MinecraftConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
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
    }

    public PacketData readPacket() throws IOException {
        int length = VarInt.readVarInt(input);

        if (length <= 0) {
            throw new IOException("Invalid packet length: " + length);
        }

        byte[] data = new byte[length];
        input.readFully(data);

        DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(data));
        int packetId = VarInt.readVarInt(dataStream);

        int remaining = length - VarInt.getVarIntSize(packetId);
        byte[] payload = new byte[remaining];
        dataStream.readFully(payload);

        return new PacketData(packetId, payload);
    }

    public void enableEncryption(byte[] sharedSecret) throws GeneralSecurityException {
        SecretKey key = new SecretKeySpec(sharedSecret, "AES");

        encryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(sharedSecret));

        decryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
        decryptCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(sharedSecret));

        this.encrypted = true;
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

    public static class PacketData {
        public final int packetId;
        public final byte[] payload;

        public PacketData(int packetId, byte[] payload) {
            this.packetId = packetId;
            this.payload = payload;
        }

        public DataInputStream getDataStream() {
            return new DataInputStream(new ByteArrayInputStream(payload));
        }
    }
}
