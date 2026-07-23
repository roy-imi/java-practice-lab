package com.roy.futurelab.model;

import com.roy.futurelab.common.Money;

import java.util.Objects;

public final class ProductSummary {
    private final String productName;
    private final int priceCents;
    private final int stock;

    public ProductSummary(String productName, int priceCents, int stock) {
        this.productName = productName;
        this.priceCents = priceCents;
        this.stock = stock;
    }

    public String getProductName() {
        return productName;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getStock() {
        return stock;
    }

    @Override
    public String toString() {
        return productName + "，价格 " + Money.format(priceCents) + "，库存 " + stock + " 件";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProductSummary)) {
            return false;
        }
        ProductSummary that = (ProductSummary) other;
        return priceCents == that.priceCents
                && stock == that.stock
                && Objects.equals(productName, that.productName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productName, priceCents, stock);
    }
}
