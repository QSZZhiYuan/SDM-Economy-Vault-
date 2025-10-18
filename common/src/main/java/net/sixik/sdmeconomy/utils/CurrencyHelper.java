package net.sixik.sdmeconomy.utils;

import dev.architectury.platform.Platform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.sixik.sdmeconomy.CustomPlayerData;
import net.sixik.sdmeconomy.SDMEconomy;
import net.sixik.sdmeconomy.currencies.BaseCurrency;
import net.sixik.sdmeconomy.currencies.CustomCurrencies;
import net.sixik.sdmeconomy.currencies.data.CurrenciesIO;
import net.sixik.sdmeconomy.currencies.data.CurrencyData;
import net.sixik.sdmeconomy.currencies.data.CurrencyPlayerData;
import net.sixik.sdmeconomy.network.client.SendCurrenciesS2C;
import net.sixik.sdmeconomy.network.client.SendCustomDataS2C;
import net.sixik.sdmeconomy.network.client.SendPlayerCurrenciesS2C;
import net.sixik.sdmeconomy.network.server.SendCreateCurrencyC2S;
import net.sixik.sdmeconomy.network.server.SendDeleteCurrencyC2S;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CurrencyHelper {

    public static ErrorCodeStruct<CurrencyData> getAllCurrency() {
        return getAllCurrency(false);
    }

    /**
     * Retrieves a list of all currencies.
     * Prioritizes server data, but if server data is unavailable, it will use client data. May return an error if no data is found.
     * @param client is get data from client
     */
    public static ErrorCodeStruct<CurrencyData> getAllCurrency(boolean client) {
        if(!client && CurrencyData.SERVER != null) {
            return new ErrorCodeStruct<>(CurrencyData.SERVER, ErrorCodes.SUCCESS);
        }

        if(CurrencyData.CLIENT != null) {
            return new ErrorCodeStruct<>(CurrencyData.CLIENT, ErrorCodes.SUCCESS);
        }

        return new ErrorCodeStruct<>(null, ErrorCodes.FAIL);
    }

    /**
     * Synchronization of player data from the server to the client. Such as currency data and player data
     */
    public static ErrorCodes syncPlayer(ServerPlayer player) {
        try {
            syncCurrencyData(player);
            syncCustomData(player);
            new SendPlayerCurrenciesS2C(player).sendTo(player);
        } catch (Exception e) {
            SDMEconomy.printStackTrace("Error when try sync player currency", e);
            return ErrorCodes.FAIL;
        }
        return ErrorCodes.SUCCESS;
    }

    public static ErrorCodeStruct<CurrencyData> getCurrencyData(boolean isClient) {
        try {
            if (isClient) {
                if (CurrencyData.CLIENT == null) {
                    return new ErrorCodeStruct<>(null, ErrorCodes.FAIL);
                }
                return new ErrorCodeStruct<>(CurrencyData.CLIENT, ErrorCodes.FAIL);
            }

            if (CurrencyData.SERVER == null) {
                return new ErrorCodeStruct<>(null, ErrorCodes.FAIL);
            }
            return new ErrorCodeStruct<>(CurrencyData.SERVER, ErrorCodes.FAIL);
        } catch (Exception e) {
            SDMEconomy.printStackTrace("Error when try get currency data", e);
            return new ErrorCodeStruct<>(null, ErrorCodes.FAIL);
        }
    }

    public static ErrorCodeStruct<LinkedList<CurrencyPlayerData.PlayerCurrency>> getCurrencyPlayerData(Player player) {
        try {
            if (CurrencyPlayerData.SERVER != null)
                return new ErrorCodeStruct<>(CurrencyPlayerData.SERVER.getPlayersCurrency(player));

            if (CurrencyPlayerData.CLIENT != null) {
                return new ErrorCodeStruct<>(CurrencyPlayerData.CLIENT.currencies);
            }
        } catch (Exception e) {
            SDMEconomy.printStackTrace("Error when try get currency for player " + player.getGameProfile().getId(), e);
            return new ErrorCodeStruct<>(new LinkedList<>(), ErrorCodes.FAIL);
        }

        return new ErrorCodeStruct<>(new LinkedList<>(), ErrorCodes.NOT_FOUND);
    }

    public static CurrencyPlayerData.Server getPlayerCurrencyServerData() {
        return CurrencyPlayerData.SERVER;
    }

    public static CurrencyPlayerData.Client getPlayerCurrencyClientData() {
        return CurrencyPlayerData.CLIENT;
    }

    public static void syncCurrencyData(ServerPlayer player) {
        new SendCurrenciesS2C(CurrenciesIO.saveToNBT(CurrencyData.SERVER.currencies)).sendTo(player);
    }

    public static void syncCurrencyData(MinecraftServer server) {
        new SendCurrenciesS2C(CurrenciesIO.saveToNBT(CurrencyData.SERVER.currencies)).sendToAll(server);
    }

    public static void createCurrencyOnClient(BaseCurrency currency) {
        new SendCreateCurrencyC2S(currency).sendToServer();
    }

    public static ErrorCodes createCurrencyOnServer(BaseCurrency currency) {
        if(CurrencyData.SERVER == null)
            return ErrorCodes.FAIL;

        CurrencyData.SERVER.currencies.add(currency);
        for (Map.Entry<UUID, LinkedList<CurrencyPlayerData.PlayerCurrency>> entry :
                CurrencyPlayerData.SERVER.playersCurrencyMap.entrySet()) {
            entry.getValue().add(new CurrencyPlayerData.PlayerCurrency(currency.copy(), currency.getDefaultValue()));
        }

        saveAll(CurrencyData.SERVER.server);

        return ErrorCodes.SUCCESS;
    }

    public static void deleteCurrencyOnClient(BaseCurrency currency) {
        new SendDeleteCurrencyC2S(currency).sendToServer();
    }

    public static ErrorCodes deleteCurrencyOnServer(BaseCurrency currency) {
        if(CurrencyData.SERVER == null)
            return ErrorCodes.FAIL;

        CurrencyData.SERVER.currencies.removeIf(s -> s.getName().equals(currency.getName()));
        for (Map.Entry<UUID, LinkedList<CurrencyPlayerData.PlayerCurrency>> entry :
                CurrencyPlayerData.SERVER.playersCurrencyMap.entrySet()) {
            entry.getValue().removeIf(s -> s.currency.getName().equals(currency.getName()));
        }

        saveAll(CurrencyData.SERVER.server);

        return ErrorCodes.SUCCESS;
    }

    public static ErrorCodes syncCurrencyFromServerToClient(MinecraftServer server) {
        if(CurrencyData.SERVER == null)
            return ErrorCodes.FAIL;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            new SendPlayerCurrenciesS2C(player).sendTo(player);
        }

        return ErrorCodes.SUCCESS;
    }

    public static UUID getPlayerUUID(Player player) {
        return player.getGameProfile().getId();
    }

    public static boolean isAdmin(Player player) {
        return player.hasPermissions(3);
    }

    public static void saveAll(MinecraftServer server) {
        saveCurrencyData();
        savePlayerData(server);
        CustomPlayerData.SERVER.save(server.getWorldPath(LevelResource.ROOT));
    }

    public static void saveCurrencyData() {
        CurrenciesIO.save(Platform.getConfigFolder(), CurrencyData.SERVER.currencies);
    }

    public static void savePlayerData(MinecraftServer server) {
        CurrenciesIO.savePlayersData(server.getWorldPath(LevelResource.ROOT), CurrencyPlayerData.SERVER.playersCurrencyMap);
    }

    public static ErrorCodeStruct<Boolean> checkNewCurrency() {
        try {
            List<BaseCurrency> check = new ArrayList<>();
            List<BaseCurrency> notFounded = new ArrayList<>();


            for (Supplier<BaseCurrency> value : CustomCurrencies.CURRENCIES.values()) {
                check.add(value.get());
            }

            for (BaseCurrency currency1 : check) {
                if (CurrencyData.SERVER.currencies.stream().noneMatch(c -> c.getName().equals(currency1.getName()))) {
                    notFounded.add(currency1.copy());
                }
            }

            CurrencyData.SERVER.currencies.addAll(notFounded);

            for (Map.Entry<UUID, LinkedList<CurrencyPlayerData.PlayerCurrency>> entry : CurrencyPlayerData.SERVER.playersCurrencyMap.entrySet()) {
                notFounded = new ArrayList<>();
                for (BaseCurrency currency1 : check) {
                    if (entry.getValue().stream().noneMatch(c -> c.currency.getName().equals(currency1.getName()))) {
                        notFounded.add(currency1.copy());
                    }
                }

                if(notFounded.isEmpty()) continue;
                entry.getValue().addAll(
                        notFounded
                                .stream()
                                .map(s -> new CurrencyPlayerData.PlayerCurrency(s, s.getDefaultValue()))
                                .toList());
            }

            return new ErrorCodeStruct<>(true, ErrorCodes.SUCCESS);
        } catch (Exception e) {
            SDMEconomy.printStackTrace("Error when check new currency", e);
            return new ErrorCodeStruct<>(false, ErrorCodes.FAIL);
        }
    }

    public static CustomPlayerData.Server getCustomServerData() {
        return CustomPlayerData.SERVER;
    }

    public static CustomPlayerData.Client getCustomClientData() {
        return CustomPlayerData.CLIENT;
    }

    public static void syncCustomData(ServerPlayer player) {
        new SendCustomDataS2C(getPlayerUUID(player)).sendTo((ServerPlayer) player);
    }

    public static ErrorCodes updateCustomData(ServerPlayer player, Consumer<CompoundTag> nbt) {
        try {
            CustomPlayerData.Data data = getCustomServerData().getPlayerCustomData(player);
            nbt.accept(data.nbt);
            new SendCustomDataS2C(getPlayerUUID(player)).sendTo((ServerPlayer) player);
            return ErrorCodes.SUCCESS;
        } catch (Exception e) {
            SDMEconomy.printStackTrace("Error when update custom data", e);
            return ErrorCodes.FAIL;
        }
    }

    public static CompoundTag getCustomData(Player player) {
        if(player.isLocalPlayer())
            return getCustomClientData().data.nbt;
        return getCustomServerData().getPlayerCustomData(player).nbt;
    }
}
