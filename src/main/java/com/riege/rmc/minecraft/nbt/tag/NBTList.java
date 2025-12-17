package com.riege.rmc.minecraft.nbt.tag;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a TAG_List in NBT format.
 * All elements in the list must be of the same type.
 */
public final class NBTList implements NBTTag {
    public static final byte TYPE_ID = 9;
    private final List<NBTTag> value;
    private final byte elementType;

    public NBTList(byte elementType) {
        this.value = new ArrayList<>();
        this.elementType = elementType;
    }

    private NBTList(byte elementType, List<NBTTag> value) {
        this.elementType = elementType;
        this.value = new ArrayList<>(value);
    }

    public NBTList add(NBTTag tag) {
        if (tag.getTypeId() != elementType && !value.isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot add " + tag.getTypeName() + " to list of type " + elementType
            );
        }
        value.add(tag);
        return this;
    }

    public NBTList addString(String str) {
        return add(new NBTString(str));
    }

    public NBTList addInt(int i) {
        return add(new NBTInt(i));
    }

    public NBTList addCompound(NBTCompound compound) {
        return add(compound);
    }

    public Optional<NBTTag> get(int index) {
        if (index < 0 || index >= value.size()) return Optional.empty();
        return Optional.of(value.get(index));
    }

    public String getString(int index) {
        return get(index)
            .filter(NBTTag::isString)
            .map(NBTTag::asString)
            .map(NBTString::value)
            .orElse("");
    }

    public Optional<NBTCompound> getCompound(int index) {
        return get(index)
            .filter(NBTTag::isCompound)
            .map(NBTTag::asCompound);
    }

    public int size() {
        return value.size();
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public byte getElementType() {
        return elementType;
    }

    public Stream<NBTTag> stream() {
        return value.stream();
    }

    public List<NBTTag> getValue() {
        return Collections.unmodifiableList(value);
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTypeName() {
        return "TAG_List";
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(elementType);
        out.writeInt(value.size());
        for (NBTTag tag : value) {
            tag.write(out);
        }
    }

    @Override
    public NBTTag copy() {
        List<NBTTag> copiedList = new ArrayList<>();
        for (NBTTag tag : value) {
            copiedList.add(tag.copy());
        }
        return new NBTList(elementType, copiedList);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
