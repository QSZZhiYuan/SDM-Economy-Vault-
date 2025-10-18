package net.sixik.sdmeconomy.network.client;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.sixik.sdmeconomy.currencies.data.CurrencyPlayerData;
import net.sixik.sdmeconomy.network.SDMEconomyNetwork;
import net.sixik.sdmeconomy.utils.CurrencyHelper;

public class SendUpdatePlayerCurrencyS2C extends BaseS2CMessage {

    private final CompoundTag nbt;

    public SendUpdatePlayerCurrencyS2C(CurrencyPlayerData.PlayerCurrency currency) {
        this.nbt = currency.serialize();
    }

    public SendUpdatePlayerCurrencyS2C(FriendlyByteBuf buf) {
        this.nbt = buf.readNbt();
    }

    @Override
    public MessageType getType() {
        return SDMEconomyNetwork.SEND_UPDATE_CURRENCY_PLAYER;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(nbt);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        CurrencyPlayerData.PlayerCurrency currency = CurrencyPlayerData.PlayerCurrency.deserialize(nbt);
        CurrencyHelper.getPlayerCurrencyClientData().updateCurrencyForce(currency);
    }
}