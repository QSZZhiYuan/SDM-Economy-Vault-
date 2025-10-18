package net.sixik.sdmeconomy.api;

import java.util.UUID;

public interface IntegrationCurrency {

    void addCurrency(UUID player, double amount);
    void setCurrency(UUID player, double amount);
    double getCurrency(UUID player);
}

