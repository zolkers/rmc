package com.riege.rmc.minecraft.nbt;

import com.riege.rmc.minecraft.nbt.io.NBTReader;
import com.riege.rmc.minecraft.nbt.tag.*;

import java.io.IOException;

/**
 * Main API for working with NBT data.
 * Provides convenient methods for common NBT operations.
 */
public final class NBT {

    /**
     * Parse NBT data from bytes.
     */
    public static NBTCompound parse(byte[] data) throws IOException {
        return NBTReader.readCompound(data);
    }

    /**
     * Extract plain text from an NBT chat component.
     * This is a specialized method for Minecraft chat messages.
     */
    public static String extractChatText(byte[] nbtData) {
        try {
            NBTCompound root = parse(nbtData);
            return extractTextRecursive(root);
        } catch (Exception e) {
            // Fallback: extract ASCII strings
            return extractAsciiStrings(nbtData);
        }
    }

    private static String extractTextRecursive(NBTTag tag) {
        StringBuilder result = new StringBuilder();

        if (tag.isString()) {
            result.append(tag.asString().value());
        } else if (tag.isCompound()) {
            NBTCompound compound = tag.asCompound();

            // Extract "text" field
            compound.get("text")
                .filter(NBTTag::isString)
                .map(NBTTag::asString)
                .map(NBTString::value)
                .ifPresent(result::append);

            // Extract "translate" field (for translation keys)
            compound.get("translate")
                .filter(NBTTag::isString)
                .map(NBTTag::asString)
                .map(NBTString::value)
                .ifPresent(result::append);

            // Process "extra" list
            compound.getList("extra").ifPresent(extra ->
                extra.stream().forEach(child ->
                    result.append(extractTextRecursive(child))
                )
            );

            // Process "with" list (for translation arguments)
            compound.getList("with").ifPresent(with ->
                with.stream().forEach(child ->
                    result.append(extractTextRecursive(child))
                )
            );
        } else if (tag.isList()) {
            tag.asList().stream().forEach(child ->
                result.append(extractTextRecursive(child))
            );
        }

        return result.toString();
    }

    private static String extractAsciiStrings(byte[] data) {
        StringBuilder result = new StringBuilder();
        for (byte b : data) {
            if (b >= 32 && b < 127) {
                result.append((char) b);
            }
        }
        return result.toString();
    }

    /**
     * Create a new compound tag builder.
     */
    public static NBTCompound compound() {
        return new NBTCompound();
    }

    /**
     * Create a new list tag builder.
     */
    public static NBTList list(byte elementType) {
        return new NBTList(elementType);
    }

    /**
     * Create a string tag.
     */
    public static NBTString string(String value) {
        return new NBTString(value);
    }

    /**
     * Create an int tag.
     */
    public static NBTInt intTag(int value) {
        return new NBTInt(value);
    }

    /**
     * Create a long tag.
     */
    public static NBTLong longTag(long value) {
        return new NBTLong(value);
    }

    /**
     * Create a boolean tag (stored as byte).
     */
    public static NBTByte bool(boolean value) {
        return new NBTByte((byte) (value ? 1 : 0));
    }
}
