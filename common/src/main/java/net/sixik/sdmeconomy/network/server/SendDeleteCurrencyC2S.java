package net.sixik.sdmeconomy.network.server;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.sixik.sdmeconomy.currencies.BaseCurrency;
import net.sixik.sdmeconomy.network.SDMEconomyNetwork;
import net.sixik.sdmeconomy.utils.CurrencyHelper;

public class SendDeleteCurrencyC2S extends BaseC2SMessage {

    private final CompoundTag nbt;

    public SendDeleteCurrencyC2S(BaseCurrency currency) {
        this.nbt = currency.serialize();
    }

    public SendDeleteCurrencyC2S(FriendlyByteBuf buf) {
        this.nbt = buf.readNbt();
    }

    @Override
    public MessageType getType() {
        return SDMEconomyNetwork.SEND_DELETE_CURRENCY;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(nbt);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if(!CurrencyHelper.isAdmin(context.getPlayer())) return;

        CurrencyHelper.deleteCurrencyOnServer(BaseCurrency.deserialize(nbt));

        CurrencyHelper.saveAll(context.getPlayer().getServer());

        for (ServerPlayer player : context.getPlayer().getServer().getPlayerList().getPlayers()) {
            CurrencyHelper.syncCurrencyData(player);
            CurrencyHelper.syncPlayer(player);
        }
    }
}
