package com.riege.rmc.minecraft.nbt.io;

import com.riege.rmc.minecraft.nbt.tag.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Reads NBT data from input streams.
 * Provides a clean API for parsing Minecraft's Named Binary Tag format.
 */
public final class NBTReader {

    /**
     * Read an NBT compound from a byte array.
     */
    public static NBTCompound readCompound(byte[] data) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            return readCompound(in);
        }
    }

    /**
     * Read an NBT compound from a stream.
     */
    public static NBTCompound readCompound(DataInputStream in) throws IOException {
        NBTCompound compound = new NBTCompound();

        while (true) {
            byte tagType = in.readByte();
            if (tagType == 0) break; // TAG_End

            String name = in.readUTF();
            NBTTag tag = readTag(tagType, in);
            compound.put(name, tag);
        }

        return compound;
    }

    /**
     * Read an NBT tag of a specific type.
     */
    public static NBTTag readTag(byte typeId, DataInputStream in) throws IOException {
        return switch (typeId) {
            case 1 -> new NBTByte(in.readByte());
            case 2 -> new NBTShort(in.readShort());
            case 3 -> new NBTInt(in.readInt());
            case 4 -> new NBTLong(in.readLong());
            case 5 -> new NBTFloat(in.readFloat());
            case 6 -> new NBTDouble(in.readDouble());
            case 7 -> {
                int length = in.readInt();
                byte[] bytes = new byte[length];
                in.readFully(bytes);
                yield new NBTByteArray(bytes);
            }
            case 8 -> new NBTString(in.readUTF());
            case 9 -> readList(in);
            case 10 -> readCompound(in);
            case 11 -> {
                int length = in.readInt();
                int[] ints = new int[length];
                for (int i = 0; i < length; i++) {
                    ints[i] = in.readInt();
                }
                yield new NBTIntArray(ints);
            }
            case 12 -> {
                int length = in.readInt();
                long[] longs = new long[length];
                for (int i = 0; i < length; i++) {
                    longs[i] = in.readLong();
                }
                yield new NBTLongArray(longs);
            }
            default -> throw new IOException("Unknown NBT tag type: " + typeId);
        };
    }

    private static NBTList readList(DataInputStream in) throws IOException {
        byte elementType = in.readByte();
        int length = in.readInt();

        NBTList list = new NBTList(elementType);
        for (int i = 0; i < length; i++) {
            list.add(readTag(elementType, in));
        }

        return list;
    }
}
