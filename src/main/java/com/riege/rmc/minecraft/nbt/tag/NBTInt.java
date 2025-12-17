package com.riege.rmc.minecraft.nbt.tag;
import java.io.DataOutputStream;
import java.io.IOException;

public record NBTInt(int value) implements NBTTag {
    public static final byte TYPE_ID = 3;

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTypeName() {
        return "TAG_Int";
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(value);
    }

    @Override
    public NBTTag copy() {
        return new NBTInt(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
