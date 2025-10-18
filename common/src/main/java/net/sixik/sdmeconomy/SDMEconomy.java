package net.sixik.sdmeconomy;

import com.mojang.logging.LogUtils;
import net.sixik.sdmeconomy.currencies.compat.impactor.ImpactorCurrency;
import net.sixik.sdmeconomy.currencies.compat.impactor.ImpactorHelper;
import net.sixik.sdmeconomy.network.SDMEconomyNetwork;
import org.slf4j.Logger;

public final class SDMEconomy {
    public static final String MOD_ID = "sdmeconomy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {

        if(ImpactorCurrency.isLoaded()) {
            ImpactorHelper.registerImpactorCurrencies();
        }

        SDMEconomyNetwork.init();
        SDMEconomyEvents.init();
    }


    public static void printStackTrace(String str, Throwable s){
        StringBuilder strBuilder = new StringBuilder(str + " " + s.getMessage());
        for (StackTraceElement stackTraceElement : s.getStackTrace()) {
            strBuilder.append("\t").append(" ").append("at").append(" ").append(stackTraceElement).append("\n");
        }
        str = strBuilder.toString();

        for (Throwable throwable : s.getSuppressed()) {
            printStackTrace(str, throwable);
        }



        Throwable ourCause = s.getCause();
        if(ourCause != null){
            printStackTrace(str, ourCause);
        }


        LOGGER.error(str);

    }
}
