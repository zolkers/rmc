package com.riege.rmc.minecraft.nbt.tag;

import java.io.DataOutputStream;
import java.io.IOException;

public record NBTByte(byte value) implements NBTTag {
    public static final byte TYPE_ID = 1;

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTypeName() {
        return "TAG_Byte";
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(value);
    }

    @Override
    public NBTTag copy() {
        return new NBTByte(value);
    }

    @Override
    public String toString() {
        return value + "b";
    }
}
