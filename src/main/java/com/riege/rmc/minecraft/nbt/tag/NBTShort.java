package com.riege.rmc.minecraft.nbt.tag;
import java.io.DataOutputStream;
import java.io.IOException;

public record NBTShort(short value) implements NBTTag {
    public static final byte TYPE_ID = 2;

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTypeName() {
        return "TAG_Short";
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeShort(value);
    }

    @Override
    public NBTTag copy() {
        return new NBTShort(value);
    }

    @Override
    public String toString() {
        return value + "s";
    }
}
