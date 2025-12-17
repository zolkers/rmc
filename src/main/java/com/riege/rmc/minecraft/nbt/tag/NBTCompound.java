package com.riege.rmc.minecraft.nbt.tag;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;


public final class NBTCompound implements NBTTag {
    public static final byte TYPE_ID = 10;
    private final Map<String, NBTTag> tags;

    public NBTCompound() {
        this.tags = new LinkedHashMap<>();
    }

    private NBTCompound(Map<String, NBTTag> tags) {
        this.tags = new LinkedHashMap<>(tags);
    }

    public NBTCompound put(String key, NBTTag value) {
        tags.put(key, value);
        return this;
    }

    public NBTCompound putByte(String key, byte value) {
        return put(key, new NBTByte(value));
    }

    public NBTCompound putShort(String key, short value) {
        return put(key, new NBTShort(value));
    }

    public NBTCompound putInt(String key, int value) {
        return put(key, new NBTInt(value));
    }

    public NBTCompound putLong(String key, long value) {
        return put(key, new NBTLong(value));
    }

    public NBTCompound putFloat(String key, float value) {
        return put(key, new NBTFloat(value));
    }

    public NBTCompound putDouble(String key, double value) {
        return put(key, new NBTDouble(value));
    }

    public NBTCompound putString(String key, String value) {
        return put(key, new NBTString(value));
    }

    public NBTCompound putBoolean(String key, boolean value) {
        return putByte(key, (byte) (value ? 1 : 0));
    }

    // Getters
    public Optional<NBTTag> get(String key) {
        return Optional.ofNullable(tags.get(key));
    }

    public String getString(String key) {
        return get(key)
            .filter(NBTTag::isString)
            .map(NBTTag::asString)
            .map(NBTString::value)
            .orElse("");
    }

    public int getInt(String key) {
        return get(key)
            .filter(NBTTag::isInt)
            .map(NBTTag::asInt)
            .map(NBTInt::value)
            .orElse(0);
    }

    public long getLong(String key) {
        return get(key)
            .filter(NBTTag::isLong)
            .map(NBTTag::asLong)
            .map(NBTLong::value)
            .orElse(0L);
    }

    public boolean getBoolean(String key) {
        return get(key)
            .filter(NBTTag::isByte)
            .map(NBTTag::asByte)
            .map(NBTByte::value)
            .map(b -> b != 0)
            .orElse(false);
    }

    public Optional<NBTCompound> getCompound(String key) {
        return get(key)
            .filter(NBTTag::isCompound)
            .map(NBTTag::asCompound);
    }

    public Optional<NBTList> getList(String key) {
        return get(key)
            .filter(NBTTag::isList)
            .map(NBTTag::asList);
    }

    public boolean contains(String key) {
        return tags.containsKey(key);
    }

    public boolean contains(String key, byte typeId) {
        return get(key)
            .map(tag -> tag.getTypeId() == typeId)
            .orElse(false);
    }

    public Set<String> keys() {
        return Collections.unmodifiableSet(tags.keySet());
    }

    public int size() {
        return tags.size();
    }

    public boolean isEmpty() {
        return tags.isEmpty();
    }

    public void remove(String key) {
        tags.remove(key);
    }

    public void clear() {
        tags.clear();
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTypeName() {
        return "TAG_Compound";
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        for (Map.Entry<String, NBTTag> entry : tags.entrySet()) {
            out.writeByte(entry.getValue().getTypeId());
            out.writeUTF(entry.getKey());
            entry.getValue().write(out);
        }
        out.writeByte(0); // TAG_End
    }

    @Override
    public NBTTag copy() {
        Map<String, NBTTag> copiedTags = new LinkedHashMap<>();
        for (Map.Entry<String, NBTTag> entry : tags.entrySet()) {
            copiedTags.put(entry.getKey(), entry.getValue().copy());
        }
        return new NBTCompound(copiedTags);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, NBTTag> entry : tags.entrySet()) {
            if (!first) sb.append(", ");
            sb.append(entry.getKey()).append(": ").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
