package com.riege.rmc.minecraft.nbt.tag;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public record NBTLongArray(long[] value) implements NBTTag {
    public static final byte TYPE_ID = 12;

    public NBTLongArray(long[] value) {
        this.value = value.clone();
    }

    @Override
    public long[] value() {
        return value.clone();
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTypeName() {
        return "TAG_Long_Array";
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(value.length);
        for (long l : value) out.writeLong(l);
    }

    @Override
    public NBTTag copy() {
        return new NBTLongArray(value);
    }

    @Override
    public String toString() {
        return Arrays.toString(value);
    }
}
