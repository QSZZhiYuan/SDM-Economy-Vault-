package net.sixik.sdmeconomy.currencies;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class CurrencySymbol {

    public String value;
    public Type type;

    public CurrencySymbol(String value) {
        this.value = value;
        var v = ResourceLocation.tryParse(value);
        if(v == null) {
            type = Type.CHAR;
            return;
        }
        type = Type.ICON;
    }

    public CurrencySymbol(String value, Type type) {
        this.value = value;
        this.type = type;
    }

    public enum Type {
        CHAR,
        ICON
    }

    public CurrencySymbol copy() {
        return new CurrencySymbol(value, type);
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("value", value);
        tag.putInt("type", type.ordinal());
        return tag;
    }

    public static CurrencySymbol deserialize(CompoundTag tag) {
        return new CurrencySymbol(tag.getString("value"), Type.values()[tag.getInt("type")]);
    }
}

