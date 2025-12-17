package com.riege.rmc.minecraft.nbt.tag;
import java.io.DataOutputStream;
import java.io.IOException;

public record NBTDouble(double value) implements NBTTag {
    public static final byte TYPE_ID = 6;

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTypeName() {
        return "TAG_Double";
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeDouble(value);
    }

    @Override
    public NBTTag copy() {
        return new NBTDouble(value);
    }

    @Override
    public String toString() {
        return value + "d";
    }
}
