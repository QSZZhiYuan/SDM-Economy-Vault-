package net.sixik.sdmeconomy.currencies.data;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.sixik.sdmeconomy.CustomPlayerData;
import net.sixik.sdmeconomy.SDMEconomy;
import net.sixik.sdmeconomy.currencies.BaseCurrency;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CurrenciesIO {

    public static class Constants {
        public static final String FOLDER = "SDMEconomy/currencies/";
        public static final String PLAYERS_FOLDER = "players/";
        public static final String PLAYER_CUSTOM_DATA = "players_custom_data/";
        public static final String FILE = "currencies.data";
        public static final String CURRENCY_ARRAY_KEY = "currencies";
        public static final String CUSTOM_DATA_KEY = "custom_data";
    }

    public static Map<UUID, LinkedList<CurrencyPlayerData.PlayerCurrency>> loadPlayersData(Path path) {
        Map<UUID, LinkedList<CurrencyPlayerData.PlayerCurrency>> playerData = Maps.newHashMap();

        Path pathFolder = path.resolve(Constants.FOLDER).resolve(Constants.PLAYERS_FOLDER);
        File fileFolder = pathFolder.toFile();

        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
            return playerData;
        }

        for (File file : fileFolder.listFiles()) {
            try {
                UUID uuid = UUID.fromString(FilenameUtils.removeExtension(file.getName()));
                CompoundTag nbt = NbtIo.read(file);
                if (nbt == null || !nbt.contains(Constants.CURRENCY_ARRAY_KEY)) continue;

                ListTag currencyArrayNBT = (ListTag) nbt.get(Constants.CURRENCY_ARRAY_KEY);
                for (Tag tag : currencyArrayNBT) {
                    if (tag instanceof CompoundTag listNBT) {
                        playerData.computeIfAbsent(uuid, k -> new LinkedList<>()).add(CurrencyPlayerData.PlayerCurrency.deserialize(listNBT));
                    }
                }

            } catch (Exception e) {
                SDMEconomy.printStackTrace("Error reading file " + file, e);
            }
        }

        return playerData;
    }

    public static void savePlayersData(Path path, Map<UUID, LinkedList<CurrencyPlayerData.PlayerCurrency>> playerData) {
        Path pathFolder = path.resolve(Constants.FOLDER).resolve(Constants.PLAYERS_FOLDER);
        File fileFolder = pathFolder.toFile();

        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }

        for (Map.Entry<UUID, LinkedList<CurrencyPlayerData.PlayerCurrency>> entry : playerData.entrySet()) {
            UUID uuid = entry.getKey();
            try {
                File file = new File(fileFolder, uuid.toString() + ".data");
                CompoundTag nbt = new CompoundTag();
                ListTag currencyArrayNBT = new ListTag();
                for (CurrencyPlayerData.PlayerCurrency currency : entry.getValue()) {
                    currencyArrayNBT.add(currency.serialize());
                }
                nbt.put(Constants.CURRENCY_ARRAY_KEY, currencyArrayNBT);
                NbtIo.write(nbt, file);
            } catch (Exception e) {
                SDMEconomy.printStackTrace("Error writing file " + uuid + ".data", e);
            }
        }
    }


    public static LinkedList<BaseCurrency> load(Path path) {
        LinkedList<BaseCurrency> currencies = new LinkedList<>();

        Path pathFolder = path.resolve(Constants.FOLDER);
        File fileFolder = pathFolder.toFile();

        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
            return currencies;
        }


        try {
            CompoundTag nbt = NbtIo.read(pathFolder.resolve(Constants.FILE).toFile());
            if (nbt != null && nbt.contains(Constants.CURRENCY_ARRAY_KEY)) {
                ListTag currencyArrayNBT = (ListTag) nbt.get(Constants.CURRENCY_ARRAY_KEY);
                for (Tag tag : currencyArrayNBT) {
                    if (tag instanceof CompoundTag listNBT) {
                        currencies.add(BaseCurrency.deserialize(listNBT));
                    }
                }
            }

        } catch (Exception e) {
            SDMEconomy.printStackTrace("Error reading file " + pathFolder, e);
        }

        return currencies;
    }

    public static void save(Path path, List<BaseCurrency> currencies) {
        Path pathFolder = path.resolve(Constants.FOLDER);
        File fileFolder = pathFolder.toFile();

        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }

        CompoundTag nbt = new CompoundTag();
        ListTag currencyArrayNBT = new ListTag();

        for (BaseCurrency currency : currencies) {
            currencyArrayNBT.add(currency.serialize());
        }

        nbt.put(Constants.CURRENCY_ARRAY_KEY, currencyArrayNBT);

        try {
            NbtIo.write(nbt, pathFolder.resolve(Constants.FILE).toFile());
        } catch (Exception e) {
            SDMEconomy.printStackTrace("Error writing file " + pathFolder, e);
        }
    }

    public static LinkedList<BaseCurrency> loadFromNBT(CompoundTag nbt) {
        LinkedList<BaseCurrency> currencies = new LinkedList<>();
        if (nbt.contains(Constants.CURRENCY_ARRAY_KEY)) {
            ListTag currencyArrayNBT = nbt.getList(Constants.CURRENCY_ARRAY_KEY, Tag.TAG_COMPOUND);
            for (Tag tag : currencyArrayNBT) {
                if (tag instanceof CompoundTag listNBT) {
                    currencies.add(BaseCurrency.deserialize(listNBT));
                }
            }
        }
        return currencies;
    }

    public static CompoundTag saveToNBT(List<BaseCurrency> currencies) {
        CompoundTag nbt = new CompoundTag();
        ListTag currencyArrayNBT = new ListTag();
        for (BaseCurrency currency : currencies) {
            currencyArrayNBT.add(currency.serialize());
        }
        nbt.put(Constants.CURRENCY_ARRAY_KEY, currencyArrayNBT);
        return nbt;
    }


    public static void saveCustomPlayerData(Path path, Map<UUID, CustomPlayerData.Data> playersData) {
        Path pathFolder = path.resolve(Constants.FOLDER).resolve(Constants.PLAYER_CUSTOM_DATA);
        File fileFolder = pathFolder.toFile();

        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }

        for (Map.Entry<UUID, CustomPlayerData.Data> entry : playersData.entrySet()) {
            UUID uuid = entry.getKey();
            try {
                File file = new File(fileFolder, uuid.toString() + ".data");
                CompoundTag nbt = new CompoundTag();
                nbt.put(Constants.CUSTOM_DATA_KEY, entry.getValue().nbt);
                NbtIo.write(nbt, file);
            } catch (Exception e) {
                SDMEconomy.printStackTrace("Error writing file " + uuid + ".data", e);
            }
        }
    }

    public static Map<UUID, CustomPlayerData.Data> loadCustomPlayerData(Path path) {
        Map<UUID, CustomPlayerData.Data> playersData = Maps.newHashMap();

        Path pathFolder = path.resolve(Constants.FOLDER).resolve(Constants.PLAYER_CUSTOM_DATA);
        File fileFolder = pathFolder.toFile();

        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
            return playersData;
        }

        for (File file : fileFolder.listFiles()) {
            try {
                UUID uuid = UUID.fromString(FilenameUtils.removeExtension(file.getName()));
                CompoundTag nbt = NbtIo.read(file);
                if (nbt == null || !nbt.contains(Constants.CUSTOM_DATA_KEY)) continue;

                CustomPlayerData.Data data = new CustomPlayerData.Data(nbt.getCompound(Constants.CUSTOM_DATA_KEY));
                playersData.put(uuid, data);
            } catch (Exception e) {
                SDMEconomy.printStackTrace("Error reading file " + file, e);
            }
        }

        return playersData;
    }
}
