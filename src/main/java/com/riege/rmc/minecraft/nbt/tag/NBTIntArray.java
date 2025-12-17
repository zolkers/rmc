package com.riege.rmc.minecraft.nbt.tag;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public record NBTIntArray(int[] value) implements NBTTag {
    public static final byte TYPE_ID = 11;

    public NBTIntArray(int[] value) {
        this.value = value.clone();
    }

    @Override
    public int[] value() {
        return value.clone();
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTypeName() {
        return "TAG_Int_Array";
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(value.length);
        for (int i : value) out.writeInt(i);
    }

    @Override
    public NBTTag copy() {
        return new NBTIntArray(value);
    }

    @Override
    public String toString() {
        return Arrays.toString(value);
    }
}
