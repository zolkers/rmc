package com.riege.rmc.minecraft.nbt.tag;

import java.io.DataOutputStream;
import java.io.IOException;

public record NBTString(String value) implements NBTTag {
    public static final byte TYPE_ID = 8;

    public NBTString(String value) {
        this.value = value == null ? "" : value;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTypeName() {
        return "TAG_String";
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(value);
    }

    @Override
    public NBTTag copy() {
        return new NBTString(value);
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NBTString other)) return false;
        return value.equals(other.value);
    }

}
