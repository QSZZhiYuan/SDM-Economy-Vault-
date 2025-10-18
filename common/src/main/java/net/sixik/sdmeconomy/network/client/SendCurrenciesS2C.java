package net.sixik.sdmeconomy.network.client;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.sixik.sdmeconomy.currencies.data.CurrencyData;
import net.sixik.sdmeconomy.network.SDMEconomyNetwork;

public class SendCurrenciesS2C extends BaseS2CMessage {

    private final CompoundTag nbt;

    public SendCurrenciesS2C(FriendlyByteBuf buf) {
        this.nbt = buf.readNbt();
    }

    public SendCurrenciesS2C(CompoundTag nbt) {
        this.nbt = nbt;
    }

    @Override
    public MessageType getType() {
        return SDMEconomyNetwork.SEND_CURRENCIES;
    }


    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(nbt);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        CurrencyData.CLIENT.reloadCurrenciesFromNetwork(nbt);
    }
}
