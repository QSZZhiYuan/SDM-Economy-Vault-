package net.sixik.sdmeconomy.currencies.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.sixik.sdmeconomy.currencies.BaseCurrency;

import java.nio.file.Path;
import java.util.LinkedList;

public class CurrencyData {

    public static CurrencyData EMPTY = new CurrencyData(new LinkedList<>());

    public static CurrencyData SERVER;
    public static CurrencyData CLIENT = new CurrencyData(new LinkedList<>());

    public MinecraftServer server;

    public LinkedList<BaseCurrency> currencies;

    public CurrencyData(LinkedList<BaseCurrency> currencies) {
        this.currencies = currencies;
    }

    public void reloadCurrenciesFromFile(Path path) {
        currencies = CurrenciesIO.load(path);
    }

    public void reloadCurrenciesFromNetwork(CompoundTag nbt) {
        currencies = CurrenciesIO.loadFromNBT(nbt);
    }

    public CompoundTag serialize() {
        return CurrenciesIO.saveToNBT(currencies);
    }

    public static CurrencyData deserialize(CompoundTag nbt) {
        return new CurrencyData(CurrenciesIO.loadFromNBT(nbt));
    }

    public CurrencyData copy() {
        return new CurrencyData(new LinkedList<>(currencies));
    }
}

