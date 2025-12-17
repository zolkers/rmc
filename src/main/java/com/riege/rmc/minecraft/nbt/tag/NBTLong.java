package com.riege.rmc.minecraft.nbt.tag;
import java.io.DataOutputStream;
import java.io.IOException;

public record NBTLong(long value) implements NBTTag {
    public static final byte TYPE_ID = 4;

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTypeName() {
        return "TAG_Long";
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeLong(value);
    }

    @Override
    public NBTTag copy() {
        return new NBTLong(value);
    }

    @Override
    public String toString() {
        return value + "L";
    }
}
