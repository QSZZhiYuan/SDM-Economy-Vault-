package net.sixik.sdmeconomy.fabric;

import net.sixik.sdmeconomy.SDMEconomy;
import net.fabricmc.api.ModInitializer;

public final class SDMEconomyFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SDMEconomy.init();
    }
}
