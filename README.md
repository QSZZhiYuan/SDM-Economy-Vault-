# SDM Economy
A library for implementing economics in SDM mods. It has flexible interaction with currencies as well as integration


## Features
- Currency registration in runtime
- The ability to set currency editing rights. (Whether the currency is being deleted or not)
- Each player has their own data file. The file can be retrieved even if the player is not online.

## For Developers

`EconomyAPI` - The main class that has access to all the necessary functions

If you need to get the players data, then use `EconomyAPI.getCustomServerData()` for server or `EconomyAPI.getCustomClientData()` for client

To interact with the currencies of the players and not only use `EconomyAPI.getPlayerCurrencyServerData()` for server or `EconomyAPI.getPlayerCurrencyClientData()` for client.

**If you are changing player data, then you need to synchronize it manually. `EconomyAPI.syncPlayer(player)`**


To add currency support for another mod, you need to implement the `net.sixik.sdmeconomy.api.IntegrationCurrency` interface. For an example, look at `net.sixik.sdmeconomy.currencies.compat.ImpactorCurrency`.
For register call 
```java
CustomCurrencies.CURRENCIES.put("YouCurrencies ID", YouCurrencies::new);
```