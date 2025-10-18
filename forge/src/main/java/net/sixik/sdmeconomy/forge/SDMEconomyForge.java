package net.sixik.sdmeconomy.forge;

import net.minecraft.server.MinecraftServer;
import net.sixik.sdmeconomy.SDMEconomy;
import net.sixik.sdmeconomy.forge.vault.VaultIntegration;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;

@Mod(SDMEconomy.MOD_ID)
public final class SDMEconomyForge {
    public SDMEconomyForge() {
        EventBuses.registerModEventBus(SDMEconomy.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        SDMEconomy.init();
        
        // Register Forge events for Vault integration
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        
        // Try to initialize Vault integration on hybrid servers
        if (VaultIntegration.initialize(server)) {
            System.out.println("[SDM-Economy] Running on hybrid server with Vault support!");
        } else {
            System.out.println("[SDM-Economy] Running on pure Forge server (no Vault)");
        }
    }
    
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        // Cleanup Vault integration
        VaultIntegration.shutdown();
    }
}
