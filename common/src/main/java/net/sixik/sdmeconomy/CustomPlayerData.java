package net.sixik.sdmeconomy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.sixik.sdmeconomy.api.EconomyAPI;
import net.sixik.sdmeconomy.currencies.data.CurrenciesIO;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomPlayerData {

    public static Server SERVER;
    public static Client CLIENT = new Client(new Data(new CompoundTag()));

    public static class Server {
        public Map<UUID, Data> playersData = new HashMap<>();

        public Data getPlayerCustomData(Player player) {
            return getPlayerCustomData(EconomyAPI.getPlayerUUID(player));
        }

        public Data getPlayerCustomData(UUID uuid) {
            createData(uuid);
            return this.playersData.getOrDefault(uuid, new Data(new CompoundTag()));
        }

        public void createData(Player player) {
            createData(EconomyAPI.getPlayerUUID(player));
        }

        public void createData(UUID player) {
            if(this.playersData.containsKey(player)) return;

            this.playersData.put(player, new Data(new CompoundTag()));
        }

        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            ListTag listTag = new ListTag();
            for (Map.Entry<UUID, Data> entry : this.playersData.entrySet()) {
                CompoundTag nbt = new CompoundTag();
                nbt.putUUID("uuid", entry.getKey());
                nbt.put("data", entry.getValue().nbt);

                listTag.add(nbt);
            }
            tag.put("playersData", listTag);

            return tag;
        }

        public static Server deserialize(CompoundTag tag) {
            Server server = new Server();
            ListTag listTag = tag.getList("playersData", 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag nbt = listTag.getCompound(i);
                UUID uuid = nbt.getUUID("uuid");
                Data data = new Data(nbt.getCompound("data"));

                server.playersData.put(uuid, data);
            }
            return server;
        }

        public void save(Path path) {
            CurrenciesIO.saveCustomPlayerData(path, playersData);
        }

        public static Server load(Path path) {
            SERVER = new Server();
            SERVER.playersData = CurrenciesIO.loadCustomPlayerData(path);
            return SERVER;
        }

    }

    public static class Client {
        public Data data;

        public Client(Data data) {
            this.data = data;
        }

        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.put("data", this.data.nbt);
            return tag;
        }

        public static Client deserialize(CompoundTag tag) {
            CompoundTag dataTag = tag.getCompound("data");
            return new Client(new Data(dataTag));
        }
    }



    public static class Data {
        public CompoundTag nbt;
        public Data(CompoundTag nbt) {
            this.nbt = nbt;
        }
    }
}

