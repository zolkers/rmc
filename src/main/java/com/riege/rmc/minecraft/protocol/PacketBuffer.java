package com.riege.rmc.minecraft.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PacketBuffer {
    private final ByteArrayOutputStream baos;
    private final DataOutputStream dos;

    public PacketBuffer() {
        this.baos = new ByteArrayOutputStream();
        this.dos = new DataOutputStream(baos);
    }

    public void writeVarInt(int value) throws IOException {
        VarInt.writeVarInt(dos, value);
    }

    public void writeString(String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length);
        dos.write(bytes);
    }

    public void writeUUID(UUID uuid) throws IOException {
        dos.writeLong(uuid.getMostSignificantBits());
        dos.writeLong(uuid.getLeastSignificantBits());
    }

    public void writeShort(int value) throws IOException {
        dos.writeShort(value);
    }

    public void writeLong(long value) throws IOException {
        dos.writeLong(value);
    }

    public void writeBoolean(boolean value) throws IOException {
        dos.writeBoolean(value);
    }

    public void writeBytes(byte[] bytes) throws IOException {
        dos.write(bytes);
    }

    public byte[] toByteArray() {
        return baos.toByteArray();
    }
}
