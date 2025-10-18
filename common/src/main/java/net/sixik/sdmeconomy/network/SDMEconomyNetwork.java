package net.sixik.sdmeconomy.network;

import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import net.sixik.sdmeconomy.SDMEconomy;
import net.sixik.sdmeconomy.network.client.SendCurrenciesS2C;
import net.sixik.sdmeconomy.network.client.SendCustomDataS2C;
import net.sixik.sdmeconomy.network.client.SendPlayerCurrenciesS2C;
import net.sixik.sdmeconomy.network.client.SendUpdatePlayerCurrencyS2C;
import net.sixik.sdmeconomy.network.server.SendCreateCurrencyC2S;
import net.sixik.sdmeconomy.network.server.SendDeleteCurrencyC2S;

public class SDMEconomyNetwork {

    private static final SimpleNetworkManager NET = SimpleNetworkManager.create(SDMEconomy.MOD_ID);

    public static final MessageType SEND_CURRENCIES = NET.registerS2C("send_currencies", SendCurrenciesS2C::new);
    public static final MessageType SEND_CURRENCIES_PLAYER = NET.registerS2C("send_currencies_player", SendPlayerCurrenciesS2C::new);
    public static final MessageType SEND_UPDATE_CURRENCY_PLAYER = NET.registerS2C("send_update_currency_player", SendUpdatePlayerCurrencyS2C::new);

    public static final MessageType SEND_UPDATE_CUSTOM_DATA = NET.registerS2C("send_update_custom_data", SendCustomDataS2C::new);

    public static final MessageType SEND_CREATE_CURRENCY = NET.registerC2S("send_create_currency", SendCreateCurrencyC2S::new);
    public static final MessageType SEND_DELETE_CURRENCY = NET.registerC2S("send_delete_currency", SendDeleteCurrencyC2S::new);


    public static void init() {}
}
