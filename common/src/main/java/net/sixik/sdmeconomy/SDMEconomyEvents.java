package net.sixik.sdmeconomy;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import net.minecraft.world.level.storage.LevelResource;
import net.sixik.sdmeconomy.api.EconomyAPI;
import net.sixik.sdmeconomy.currencies.data.CurrencyData;
import net.sixik.sdmeconomy.currencies.data.CurrencyPlayerData;
import net.sixik.sdmeconomy.utils.CurrencyHelper;

import java.util.LinkedList;

public class SDMEconomyEvents {

    public static void init() {
        PlayerEvent.PLAYER_JOIN.register(EconomyAPI::syncPlayer);

        TickEvent.SERVER_POST.register((server) -> {
            if(server.getTickCount() % 12000 == 0) {
                EconomyAPI.saveAll(server);
            }
        });

        LifecycleEvent.SERVER_STARTED.register((server) -> {
            CurrencyData.SERVER = new CurrencyData(new LinkedList<>());
            CurrencyData.SERVER.server = server;
            CurrencyData.SERVER.reloadCurrenciesFromFile(Platform.getConfigFolder());
            CurrencyPlayerData.load(server);

            CurrencyHelper.checkNewCurrency();

            CustomPlayerData.Server.load(server.getWorldPath(LevelResource.ROOT));

            EconomyAPI.saveAll(server);
        });

        LifecycleEvent.SERVER_STOPPED.register(EconomyAPI::saveAll);
    }
}
