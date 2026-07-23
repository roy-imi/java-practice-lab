package com.roy.futurelab.model;

import com.roy.futurelab.common.Money;

public final class CheckoutSummary {
    private final int originalPriceCents;
    private final int discountPercent;
    private final int deliveryFeeCents;
    private final int totalCents;

    public CheckoutSummary(int originalPriceCents,
                           int discountPercent,
                           int deliveryFeeCents) {
        this.originalPriceCents = originalPriceCents;
        this.discountPercent = discountPercent;
        this.deliveryFeeCents = deliveryFeeCents;
        this.totalCents = originalPriceCents * (100 - discountPercent) / 100
                + deliveryFeeCents;
    }

    public int getOriginalPriceCents() {
        return originalPriceCents;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public int getDeliveryFeeCents() {
        return deliveryFeeCents;
    }

    public int getTotalCents() {
        return totalCents;
    }

    @Override
    public String toString() {
        return "原价 " + Money.format(originalPriceCents)
                + "，会员折扣 " + discountPercent + "%"
                + "，运费 " + Money.format(deliveryFeeCents)
                + "，应付 " + Money.format(totalCents);
    }
}
