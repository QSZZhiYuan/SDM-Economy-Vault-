package net.sixik.sdmeconomy.currencies.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.sixik.sdmeconomy.api.IntegrationCurrency;
import net.sixik.sdmeconomy.currencies.BaseCurrency;
import net.sixik.sdmeconomy.utils.CurrencyHelper;
import net.sixik.sdmeconomy.utils.ErrorCodeStruct;
import net.sixik.sdmeconomy.utils.ErrorCodes;

import java.util.*;
import java.util.stream.Collectors;

public class CurrencyPlayerData {

    public static Server SERVER;
    public static Client CLIENT = new Client();


    public static void load(MinecraftServer server) {
        SERVER = new Server();
        SERVER.server = server;
        SERVER.playersCurrencyMap = CurrenciesIO.loadPlayersData(server.getWorldPath(LevelResource.ROOT));
    }

    public static void save(MinecraftServer server) {
        if(SERVER == null)
            throw new IllegalStateException("CurrencyData.SERVER is null");

        CurrenciesIO.savePlayersData(server.getWorldPath(LevelResource.ROOT), SERVER.playersCurrencyMap);
    }

    public static class Server {
        public MinecraftServer server;

        public Map<UUID, LinkedList<PlayerCurrency>> playersCurrencyMap = new HashMap<>();

        public ErrorCodes addCurrencyValue(Player player, String currencyName, double value) {
            return addCurrencyValue(CurrencyHelper.getPlayerUUID(player), currencyName, value);
        }

        public ErrorCodes addCurrencyValue(UUID uuid, String currencyName, double value) {
            LinkedList<PlayerCurrency> playerCurrencies = getPlayersCurrency(uuid);
            Optional<PlayerCurrency> currencyOpt = playerCurrencies.stream()
                    .filter(c -> c.currency.getName().equalsIgnoreCase(currencyName))
                    .findFirst();

            if (currencyOpt.isPresent()) {
                var cur = currencyOpt.get();
                if(cur.currency instanceof IntegrationCurrency iIntegrationCurrency) {
                    iIntegrationCurrency.addCurrency(uuid, value);

                    currencyOpt.get().balance = iIntegrationCurrency.getCurrency(uuid);
                }
                else
                    currencyOpt.get().balance += value;

                save(server);
                return ErrorCodes.SUCCESS;
            } else {
                return ErrorCodes.NOT_FOUND;
            }
        }

        public ErrorCodes setCurrencyValue(Player player, String currencyName, double value) {
            return setCurrencyValue(CurrencyHelper.getPlayerUUID(player), currencyName, value);
        }

        public ErrorCodes setCurrencyValue(UUID player, String currencyName, double value) {
            LinkedList<PlayerCurrency> playerCurrencies = getPlayersCurrency(player);
            Optional<PlayerCurrency> currencyOpt = playerCurrencies.stream()
                    .filter(c -> c.currency.getName().equalsIgnoreCase(currencyName))
                    .findFirst();

            if (currencyOpt.isPresent()) {

                var cur = currencyOpt.get();
                if(cur.currency instanceof IntegrationCurrency iIntegrationCurrency) {
                    iIntegrationCurrency.setCurrency(player, value);

                    currencyOpt.get().balance = iIntegrationCurrency.getCurrency(player);
                }
                else
                    currencyOpt.get().balance = value;



                save(server);
                return ErrorCodes.SUCCESS;
            } else {
                return ErrorCodes.NOT_FOUND;
            }
        }

        public ErrorCodeStruct<Double> getBalance(Player player, String currencyName) {
            return getBalance(CurrencyHelper.getPlayerUUID(player), currencyName);
        }

        public ErrorCodeStruct<Double> getBalance(UUID player, String currencyName) {
            LinkedList<PlayerCurrency> playerCurrencies = getPlayersCurrency(player);
            Optional<PlayerCurrency> currencyOpt = playerCurrencies.stream()
                    .filter(c -> c.currency.getName().equalsIgnoreCase(currencyName))
                    .findFirst();

            if(currencyOpt.isPresent()) {
                var cur = currencyOpt.get();
                if(cur.currency instanceof IntegrationCurrency iIntegrationCurrency) {
                    return new ErrorCodeStruct<>(iIntegrationCurrency.getCurrency(player), ErrorCodes.SUCCESS);
                }
                else
                    return new ErrorCodeStruct<>(cur.balance, ErrorCodes.SUCCESS);
            }


            return new ErrorCodeStruct<>(0d, ErrorCodes.NOT_FOUND);
        }

        public LinkedList<PlayerCurrency> getPlayersCurrency(Player player) {
            return getPlayersCurrency(CurrencyHelper.getPlayerUUID(player));
        }

        public LinkedList<PlayerCurrency> getPlayersCurrency(UUID playerID) {
            if(!playersCurrencyMap.containsKey(playerID)) {
                CurrencyPlayerData.SERVER.newPlayer(playerID);
            }

            return playersCurrencyMap.computeIfAbsent(playerID, k -> new LinkedList<>());
        }

        public Optional<PlayerCurrency> getPlayerCurrency(Player player, String currencyName) {
            return getPlayerCurrency(CurrencyHelper.getPlayerUUID(player), currencyName);
        }

        public Optional<PlayerCurrency> getPlayerCurrency(UUID playerID, String currencyName) {
            return getPlayersCurrency(playerID).stream()
                    .filter(c -> c.currency.getName().equalsIgnoreCase(currencyName))
                    .findFirst();
        }

        public LinkedList<PlayerCurrency> getPlayerUnlockedCurrency(Player player) {
            return getPlayersCurrency(CurrencyHelper.getPlayerUUID(player));
        }

        public LinkedList<PlayerCurrency> getPlayerUnlockedCurrency(UUID player) {
            return getPlayersCurrency(player).stream()
                    .filter(c -> !c.isLocked)
                    .collect(Collectors.toCollection(LinkedList::new));
        }

        public LinkedList<PlayerCurrency> getPlayerLockedCurrency(Player player) {
            return getPlayersCurrency(CurrencyHelper.getPlayerUUID(player));
        }

        public LinkedList<PlayerCurrency> getPlayerLockedCurrency(UUID player) {
            return getPlayersCurrency(player).stream()
                    .filter(c ->  c.isLocked)
                    .collect(Collectors.toCollection(LinkedList::new));
        }

        public ErrorCodes lockCurrency(Player player, String currencyName, boolean value) {
            return lockCurrency(CurrencyHelper.getPlayerUUID(player), currencyName, value);
        }

        public ErrorCodes lockCurrency(UUID player, String currencyName, boolean value) {
            LinkedList<PlayerCurrency> playerCurrencies = getPlayersCurrency(player);
            Optional<PlayerCurrency> currencyOpt = playerCurrencies.stream()
                    .filter(c -> c.currency.getName().equalsIgnoreCase(currencyName))
                    .findFirst();

            if (currencyOpt.isPresent()) {
                currencyOpt.get().isLocked = value;
                save(server);
                return ErrorCodes.SUCCESS;
            }

            return ErrorCodes.NOT_FOUND;
        }

        public void newPlayer(Player player) {
            newPlayer(CurrencyHelper.getPlayerUUID(player));
        }

        public void newPlayer(UUID playerID) {
            if(CurrencyData.SERVER == null)
                throw new IllegalStateException("CurrencyData.SERVER is null");

            LinkedList<PlayerCurrency> currencies = new LinkedList<>();

            CurrencyData.SERVER.currencies.stream()
                    .map(BaseCurrency::copy)
                    .forEach(cur -> currencies.add(new PlayerCurrency(cur, cur.getDefaultValue())));

            playersCurrencyMap.put(playerID, currencies);

            save(server);
        }
    }

    public static class Client {
        public LinkedList<PlayerCurrency> currencies = new LinkedList<>();

        public Optional<PlayerCurrency> getCurrency(String currencyName) {
            return currencies.stream()
                    .filter(c -> c.currency.getName().equalsIgnoreCase(currencyName))
                    .findFirst();
        }

        public double getBalance(String currencyName) {
            return getCurrency(currencyName).map(s -> s.balance).orElse(0.0);
        }

        public boolean hasCurrency(String currencyName) {
            return getCurrency(currencyName).isPresent();
        }

        public ErrorCodeStruct<Boolean> isCurrencyLocked(String currencyName) {
            Optional<PlayerCurrency> currency = getCurrency(currencyName);
            return currency
                    .map(playerCurrency -> new ErrorCodeStruct<>(playerCurrency.isLocked, ErrorCodes.SUCCESS))
                    .orElseGet(() -> new ErrorCodeStruct<>(false, ErrorCodes.NOT_FOUND));
        }

        public LinkedList<PlayerCurrency> getAllLockedCurrency() {
            return currencies.stream()
                    .filter(c -> c.isLocked)
                    .collect(Collectors.toCollection(LinkedList::new));
        }

        public LinkedList<PlayerCurrency> getAllUnlockedCurrency() {
            return currencies.stream()
                    .filter(c ->!c.isLocked)
                    .collect(Collectors.toCollection(LinkedList::new));
        }


        public ErrorCodeStruct<Boolean> updateCurrency(CurrencyPlayerData.PlayerCurrency currency) {
            return updateCurrency(currency.currency.getName(), currency.balance, currency.isLocked);
        }

        public ErrorCodeStruct<Boolean> updateCurrency(String currencyName, double value, boolean isLocked) {
            Optional<PlayerCurrency> currency = getCurrency(currencyName);
            if(currency.isPresent()) {
                currency.get().balance = value;
                currency.get().isLocked = isLocked;
                return new ErrorCodeStruct<>(true, ErrorCodes.SUCCESS);
            }
            return new ErrorCodeStruct<>(false, ErrorCodes.NOT_FOUND);
        }

        public ErrorCodes updateCurrencyForce(CurrencyPlayerData.PlayerCurrency currency) {
            return updateCurrencyForce(currency.currency.getName(), currency.balance, currency.isLocked);
        }

        public ErrorCodes updateCurrencyForce(String currencyName, double value, boolean isLocked) {
            Optional<PlayerCurrency> currency = getCurrency(currencyName);
            if(currency.isPresent()) {
                currency.get().balance = value;
                currency.get().isLocked = isLocked;
                return ErrorCodes.SUCCESS;
            } else {
                Optional<BaseCurrency> cur = CurrencyData.CLIENT.currencies.stream().filter(s -> s.getName().equals(currencyName)).findFirst();
                if(cur.isPresent()) {
                    PlayerCurrency ne = new PlayerCurrency(cur.get(), value);
                    ne.isLocked = isLocked;
                    currencies.add(ne);
                    return ErrorCodes.SUCCESS;
                }
                return ErrorCodes.NOT_FOUND;
            }
        }

        public void load(CompoundTag nbt) {
            currencies.clear();
            if(nbt.contains(CurrenciesIO.Constants.CURRENCY_ARRAY_KEY)) {
                ListTag currencyList = (ListTag) nbt.get(CurrenciesIO.Constants.CURRENCY_ARRAY_KEY);
                for(int i = 0; i < currencyList.size(); i++) {
                    CompoundTag curTag = currencyList.getCompound(i);
                    currencies.add(PlayerCurrency.deserialize(curTag));
                }
            }
        }
    }

    public static class PlayerCurrency {
        public BaseCurrency currency;
        public double balance;
        public boolean isLocked = false;

        public PlayerCurrency(BaseCurrency currency, double balance) {
            this.currency = currency;
            this.balance = balance;
        }

        public PlayerCurrency setLocked(boolean locked) {
            isLocked = locked;
            return this;
        }

        public static PlayerCurrency deserialize(CompoundTag nbt) {
            return new PlayerCurrency(
                    BaseCurrency.deserialize(nbt.getCompound("currency")),
                    nbt.getDouble("balance")).setLocked(nbt.contains("isLocked"));
        }

        public CompoundTag serialize() {
            CompoundTag nbt = new CompoundTag();
            nbt.put("currency", currency.serialize());
            nbt.putDouble("balance", balance);

            if(isLocked) nbt.putBoolean("isLocked", true);
            return nbt;
        }
    }
}
