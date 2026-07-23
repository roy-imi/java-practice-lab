package com.roy.futurelab.model;

import com.roy.futurelab.common.Money;

public final class ShopQuote {
    private final String shopName;
    private final int priceCents;

    public ShopQuote(String shopName, int priceCents) {
        this.shopName = shopName;
        this.priceCents = priceCents;
    }

    public String getShopName() {
        return shopName;
    }

    public int getPriceCents() {
        return priceCents;
    }

    @Override
    public String toString() {
        return shopName + " 报价 " + Money.format(priceCents);
    }
}
