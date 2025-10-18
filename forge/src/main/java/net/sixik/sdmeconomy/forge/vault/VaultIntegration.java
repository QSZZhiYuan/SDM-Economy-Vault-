package net.sixik.sdmeconomy.forge.vault;

import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

/**
 * Vault Integration Manager for SDM-Economy
 * Detects hybrid server environment and registers economy provider
 */
public class VaultIntegration {
    
    private static boolean vaultEnabled = false;
    private static SDMVaultEconomy economyProvider = null;
    
    /**
     * Initialize Vault integration if running on hybrid server
     * @param server Minecraft server instance
     * @return true if Vault integration is active
     */
    public static boolean initialize(MinecraftServer server) {
        // Check if running on hybrid server (Arclight/Mohist)
        if (!isHybridServer()) {
            return false;
        }
        
        // Check if Vault plugin is present
        if (!hasVaultPlugin()) {
            return false;
        }
        
        try {
            // Create economy provider for basic money
            economyProvider = new SDMVaultEconomy(
                server,
                "basic_money",    // Currency ID
                "Money",          // Currency name
                "$"               // Currency symbol
            );
            
            // Register with Bukkit services
            Bukkit.getServicesManager().register(
                net.milkbowl.vault.economy.Economy.class,
                economyProvider,
                getSDMPlugin(),
                ServicePriority.Highest
            );
            
            vaultEnabled = true;
            logInfo("SDM-Economy Vault integration enabled successfully!");
            return true;
            
        } catch (Exception e) {
            logError("Failed to initialize Vault integration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if running on hybrid server (Arclight/Mohist)
     */
    private static boolean isHybridServer() {
        try {
            // Try to access Bukkit classes
            Class.forName("org.bukkit.Bukkit");
            Class.forName("org.bukkit.Server");
            return Bukkit.getServer() != null;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return false;
        }
    }
    
    /**
     * Check if Vault plugin is installed
     */
    private static boolean hasVaultPlugin() {
        try {
            org.bukkit.plugin.Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");
            return vaultPlugin != null && vaultPlugin.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get or create dummy plugin for service registration
     */
    private static org.bukkit.plugin.Plugin getSDMPlugin() {
        // Try to find existing SDM plugin wrapper
        org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().getPlugin("SDM-Economy");
        
        if (plugin == null) {
            // Use a proxy plugin if available, or Vault itself
            plugin = Bukkit.getPluginManager().getPlugin("Vault");
        }
        
        return plugin;
    }
    
    /**
     * Shutdown Vault integration
     */
    public static void shutdown() {
        if (vaultEnabled && economyProvider != null) {
            try {
                Bukkit.getServicesManager().unregister(
                    net.milkbowl.vault.economy.Economy.class, 
                    economyProvider
                );
                logInfo("SDM-Economy Vault integration disabled");
            } catch (Exception e) {
                logError("Error during Vault shutdown: " + e.getMessage());
            }
            
            vaultEnabled = false;
            economyProvider = null;
        }
    }
    
    public static boolean isEnabled() {
        return vaultEnabled;
    }
    
    public static SDMVaultEconomy getEconomyProvider() {
        return economyProvider;
    }
    
    // Logging utilities
    private static void logInfo(String message) {
        System.out.println("[SDM-Economy Vault] " + message);
    }
    
    private static void logError(String message) {
        System.err.println("[SDM-Economy Vault] " + message);
    }
}
