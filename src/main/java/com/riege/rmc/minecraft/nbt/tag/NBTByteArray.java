package com.riege.rmc.minecraft.nbt.tag;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public record NBTByteArray(byte[] value) implements NBTTag {
    public static final byte TYPE_ID = 7;

    public NBTByteArray(byte[] value) {
        this.value = value.clone();
    }

    @Override
    public byte[] value() {
        return value.clone();
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTypeName() {
        return "TAG_Byte_Array";
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(value.length);
        out.write(value);
    }

    @Override
    public NBTTag copy() {
        return new NBTByteArray(value);
    }

    @Override
    public String toString() {
        return Arrays.toString(value);
    }
}
