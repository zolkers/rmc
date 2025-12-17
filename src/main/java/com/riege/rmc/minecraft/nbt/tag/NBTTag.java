package com.riege.rmc.minecraft.nbt.tag;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Base interface for all NBT tags.
 * Provides a fluent API for working with Minecraft's Named Binary Tag format.
 */
public interface NBTTag {

    /**
     * Get the type ID of this tag.
     */
    byte getTypeId();

    /**
     * Get the name of this tag type.
     */
    String getTypeName();

    /**
     * Write this tag to an output stream.
     */
    void write(DataOutputStream out) throws IOException;

    /**
     * Get a copy of this tag.
     */
    NBTTag copy();

    // Type check methods
    default boolean isByte() { return this instanceof NBTByte; }
    default boolean isShort() { return this instanceof NBTShort; }
    default boolean isInt() { return this instanceof NBTInt; }
    default boolean isLong() { return this instanceof NBTLong; }
    default boolean isFloat() { return this instanceof NBTFloat; }
    default boolean isDouble() { return this instanceof NBTDouble; }
    default boolean isString() { return this instanceof NBTString; }
    default boolean isList() { return this instanceof NBTList; }
    default boolean isCompound() { return this instanceof NBTCompound; }
    default boolean isByteArray() { return this instanceof NBTByteArray; }
    default boolean isIntArray() { return this instanceof NBTIntArray; }
    default boolean isLongArray() { return this instanceof NBTLongArray; }

    // Type cast methods
    default NBTByte asByte() { return (NBTByte) this; }
    default NBTShort asShort() { return (NBTShort) this; }
    default NBTInt asInt() { return (NBTInt) this; }
    default NBTLong asLong() { return (NBTLong) this; }
    default NBTFloat asFloat() { return (NBTFloat) this; }
    default NBTDouble asDouble() { return (NBTDouble) this; }
    default NBTString asString() { return (NBTString) this; }
    default NBTList asList() { return (NBTList) this; }
    default NBTCompound asCompound() { return (NBTCompound) this; }
    default NBTByteArray asByteArray() { return (NBTByteArray) this; }
    default NBTIntArray asIntArray() { return (NBTIntArray) this; }
    default NBTLongArray asLongArray() { return (NBTLongArray) this; }
}
