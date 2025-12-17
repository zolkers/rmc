package com.riege.rmc.minecraft.nbt.tag;
import java.io.DataOutputStream;
import java.io.IOException;

public record NBTFloat(float value) implements NBTTag {
    public static final byte TYPE_ID = 5;

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTypeName() {
        return "TAG_Float";
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeFloat(value);
    }

    @Override
    public NBTTag copy() {
        return new NBTFloat(value);
    }

    @Override
    public String toString() {
        return value + "f";
    }
}
