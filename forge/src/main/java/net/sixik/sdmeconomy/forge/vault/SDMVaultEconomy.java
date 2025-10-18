package net.sixik.sdmeconomy.forge.vault;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.sixik.sdmeconomy.adv.PlayerMoneyData;
import net.sixik.sdmeconomy.api.CurrencyHelper;
import net.sixik.sdmeconomy.common.cap.MoneyData;
import net.sixik.sdmeconomy.common.currency.AbstractCurrency;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * High-performance Vault Economy implementation for SDM-Economy
 * Provides direct integration without reflection overhead
 * Supports both online and offline players via persistent storage
 */
public class SDMVaultEconomy implements Economy {
    
    private final MinecraftServer server;
    private final String currencyId;
    private final String currencyName;
    private final String currencySymbol;
    
    public SDMVaultEconomy(MinecraftServer server, String currencyId, String currencyName, String currencySymbol) {
        this.server = server;
        this.currencyId = currencyId;
        this.currencyName = currencyName;
        this.currencySymbol = currencySymbol;
    }
    
    // ============ Utility Methods ============
    
    private ServerPlayer getServerPlayer(OfflinePlayer player) {
        if (player.isOnline()) {
            return server.getPlayerList().getPlayer(player.getUniqueId());
        }
        return null;
    }
    
    /**
     * Get MoneyData for any player (online or offline)
     * Uses persistent storage cache with thread-safe access
     */
    private MoneyData getMoneyData(UUID playerId) {
        if (PlayerMoneyData.SERVER == null || PlayerMoneyData.SERVER.PLAYER_MONEY == null) {
            return null;
        }
        
        synchronized (PlayerMoneyData.SERVER.PLAYER_MONEY) {
            MoneyData data = PlayerMoneyData.SERVER.PLAYER_MONEY.get(playerId);
            if (data == null) {
                data = new MoneyData();
                data.loadAllCurrencies();
                PlayerMoneyData.SERVER.PLAYER_MONEY.put(playerId, data);
            }
            return data;
        }
    }
    
    /**
     * Get balance from MoneyData for specific currency
     */
    private long getBalanceFromData(MoneyData data, String currencyId) {
        if (data == null) return 0;
        
        for (AbstractCurrency currency : data.currencies) {
            if (Objects.equals(currency.getID(), currencyId)) {
                return currency.moneys;
            }
        }
        return 0;
    }
    
    /**
     * Set balance in MoneyData for specific currency
     */
    private boolean setBalanceInData(MoneyData data, String currencyId, long amount) {
        if (data == null) return false;
        
        for (AbstractCurrency currency : data.currencies) {
            if (Objects.equals(currency.getID(), currencyId)) {
                currency.moneys = amount;
                return true;
            }
        }
        return false;
    }
    
    /**
     * Convert double to long using floor (truncate), not round
     * Prevents overdrawing when withdrawing fractional amounts
     */
    private long doubleToLong(double amount) {
        return (long) Math.floor(amount);
    }
    
    private double longToDouble(long amount) {
        return (double) amount;
    }
    
    // ============ Economy Info Methods ============
    
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "SDM-Economy";
    }

    @Override
    public boolean hasBankSupport() {
        return false; // Banks not supported in SDM-Economy
    }

    @Override
    public int fractionalDigits() {
        return 0; // SDM-Economy uses long integers, no decimals
    }

    @Override
    public String format(double amount) {
        return currencySymbol + Math.round(amount);
    }

    @Override
    public String currencyNamePlural() {
        return currencyName + "s";
    }

    @Override
    public String currencyNameSingular() {
        return currencyName;
    }
    
    // ============ Account Methods ============

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return player != null && player.getUniqueId() != null;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player); // SDM-Economy is global, not per-world
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        // Accounts are created automatically when player joins
        return hasAccount(player);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }
    
    // ============ Balance Methods ============

    @Override
    public double getBalance(OfflinePlayer player) {
        if (player == null || player.getUniqueId() == null) {
            return 0.0;
        }
        
        ServerPlayer serverPlayer = getServerPlayer(player);
        if (serverPlayer != null) {
            return longToDouble(CurrencyHelper.getMoney(serverPlayer, currencyId));
        }
        
        MoneyData data = getMoneyData(player.getUniqueId());
        return longToDouble(getBalanceFromData(data, currencyId));
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player); // Global economy, world parameter ignored
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }
    
    // ============ Transaction Methods ============

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, 
                "Cannot withdraw negative amount");
        }
        
        if (player == null || player.getUniqueId() == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, 
                "Invalid player");
        }
        
        UUID playerId = player.getUniqueId();
        long withdrawAmount = doubleToLong(amount);
        
        ServerPlayer serverPlayer = getServerPlayer(player);
        if (serverPlayer != null) {
            long currentBalance = CurrencyHelper.getMoney(serverPlayer, currencyId);
            if (currentBalance < withdrawAmount) {
                return new EconomyResponse(0, longToDouble(currentBalance), 
                    EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
            }
            CurrencyHelper.setMoney(serverPlayer, currencyId, currentBalance - withdrawAmount);
            long newBalance = CurrencyHelper.getMoney(serverPlayer, currencyId);
            return new EconomyResponse(amount, longToDouble(newBalance), 
                EconomyResponse.ResponseType.SUCCESS, null);
        }
        
        MoneyData data = getMoneyData(playerId);
        
        synchronized (PlayerMoneyData.SERVER.PLAYER_MONEY) {
            long currentBalance = getBalanceFromData(data, currencyId);
            
            if (currentBalance < withdrawAmount) {
                return new EconomyResponse(0, longToDouble(currentBalance), 
                    EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
            }
            
            setBalanceInData(data, currencyId, currentBalance - withdrawAmount);
            PlayerMoneyData.savePlayer(playerId, server);
            
            long newBalance = getBalanceFromData(data, currencyId);
            return new EconomyResponse(amount, longToDouble(newBalance), 
                EconomyResponse.ResponseType.SUCCESS, null);
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, 
                "Cannot deposit negative amount");
        }
        
        if (player == null || player.getUniqueId() == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, 
                "Invalid player");
        }
        
        UUID playerId = player.getUniqueId();
        long depositAmount = doubleToLong(amount);
        
        ServerPlayer serverPlayer = getServerPlayer(player);
        if (serverPlayer != null) {
            CurrencyHelper.addMoney(serverPlayer, currencyId, depositAmount);
            long newBalance = CurrencyHelper.getMoney(serverPlayer, currencyId);
            return new EconomyResponse(amount, longToDouble(newBalance), 
                EconomyResponse.ResponseType.SUCCESS, null);
        }
        
        MoneyData data = getMoneyData(playerId);
        
        synchronized (PlayerMoneyData.SERVER.PLAYER_MONEY) {
            long currentBalance = getBalanceFromData(data, currencyId);
            setBalanceInData(data, currencyId, currentBalance + depositAmount);
            PlayerMoneyData.savePlayer(playerId, server);
            
            long newBalance = getBalanceFromData(data, currencyId);
            return new EconomyResponse(amount, longToDouble(newBalance), 
                EconomyResponse.ResponseType.SUCCESS, null);
        }
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }
    
    // ============ Bank Methods (Not Supported) ============

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, 
            "SDM-Economy does not support banks");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, 
            "SDM-Economy does not support banks");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, 
            "SDM-Economy does not support banks");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, 
            "SDM-Economy does not support banks");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, 
            "SDM-Economy does not support banks");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, 
            "SDM-Economy does not support banks");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, 
            "SDM-Economy does not support banks");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, 
            "SDM-Economy does not support banks");
    }

    @Override
    public List<String> getBanks() {
        return List.of(); // No banks supported
    }
    
    // ============ Deprecated Methods (Redirect to OfflinePlayer variants) ============

    @Override
    @Deprecated
    public boolean hasAccount(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return hasAccount(player);
    }

    @Override
    @Deprecated
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    @Deprecated
    public double getBalance(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return getBalance(player);
    }

    @Override
    @Deprecated
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    @Deprecated
    public boolean has(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return has(player, amount);
    }

    @Override
    @Deprecated
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return withdrawPlayer(player, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return depositPlayer(player, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    @Deprecated
    public boolean createPlayerAccount(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return createPlayerAccount(player);
    }

    @Override
    @Deprecated
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override
    @Deprecated
    public EconomyResponse createBank(String name, String player) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
        return createBank(name, offlinePlayer);
    }

    @Override
    @Deprecated
    public EconomyResponse isBankOwner(String name, String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return isBankOwner(name, player);
    }

    @Override
    @Deprecated
    public EconomyResponse isBankMember(String name, String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return isBankMember(name, player);
    }
}
