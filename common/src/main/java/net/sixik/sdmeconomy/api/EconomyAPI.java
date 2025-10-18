package net.sixik.sdmeconomy.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.sixik.sdmeconomy.CustomPlayerData;
import net.sixik.sdmeconomy.currencies.BaseCurrency;
import net.sixik.sdmeconomy.currencies.data.CurrencyData;
import net.sixik.sdmeconomy.currencies.data.CurrencyPlayerData;
import net.sixik.sdmeconomy.utils.CurrencyHelper;
import net.sixik.sdmeconomy.utils.ErrorCodeStruct;
import net.sixik.sdmeconomy.utils.ErrorCodes;

import java.util.LinkedList;
import java.util.UUID;
import java.util.function.Consumer;

public class EconomyAPI {

    public static CustomPlayerData.Server getCustomServerData() {
        return CustomPlayerData.SERVER;
    }

    public static CustomPlayerData.Client getCustomClientData() {
        return CustomPlayerData.CLIENT;
    }

    public static CompoundTag getCustomData(Player player) {
        return CurrencyHelper.getCustomData(player);
    }

    public static ErrorCodes updateCustomData(ServerPlayer player, Consumer<CompoundTag> nbt) {
        return CurrencyHelper.updateCustomData(player, nbt);
    }

    public static void createCurrencyOnClient(BaseCurrency currency) {
        CurrencyHelper.createCurrencyOnClient(currency);
    }

    public static ErrorCodes createCurrencyOnServer(BaseCurrency currency) {
        return CurrencyHelper.createCurrencyOnServer(currency);
    }

    public static void deleteCurrencyOnClient(BaseCurrency currency) {
        CurrencyHelper.deleteCurrencyOnClient(currency);
    }

    public static ErrorCodes deleteCurrencyOnServer(BaseCurrency currency) {
        return CurrencyHelper.deleteCurrencyOnServer(currency);
    }

    public static ErrorCodeStruct<CurrencyData> getCurrencyData(boolean isClient) {
        return CurrencyHelper.getCurrencyData(isClient);
    }

    public static ErrorCodeStruct<LinkedList<CurrencyPlayerData.PlayerCurrency>> getCurrencyPlayerData(Player player) {
        return CurrencyHelper.getCurrencyPlayerData(player);
    }

    public static ErrorCodeStruct<CurrencyData> getAllCurrency() {
        return CurrencyHelper.getAllCurrency();
    }

    public static CurrencyPlayerData.Server getPlayerCurrencyServerData() {
        return CurrencyPlayerData.SERVER;
    }

    public static CurrencyPlayerData.Client getPlayerCurrencyClientData() {
        return CurrencyPlayerData.CLIENT;
    }

    public static ErrorCodes syncPlayer(ServerPlayer player) {
        return CurrencyHelper.syncPlayer(player);
    }

    public static ErrorCodes syncCurrencyFromServerToClient(MinecraftServer server) {
        return CurrencyHelper.syncCurrencyFromServerToClient(server);
    }

    public static void syncCurrencyData(ServerPlayer player) {
        CurrencyHelper.syncCurrencyData(player);
    }

    public static void syncCurrencyData(MinecraftServer server) {
        CurrencyHelper.syncCurrencyData(server);
    }

    public static UUID getPlayerUUID(Player player) {
        return CurrencyHelper.getPlayerUUID(player);
    }

    public static boolean isAdmin(Player player) {
        return CurrencyHelper.isAdmin(player);
    }

    public static void saveAll(MinecraftServer server) {
        CurrencyHelper.saveAll(server);
    }

    public static void saveCurrencyData() {
        CurrencyHelper.saveCurrencyData();
    }

    public static void savePlayerData(MinecraftServer server) {
        CurrencyHelper.savePlayerData(server);
    }
}
