package com.roy.lambdalab.model;

public final class Product {
    private final String name;
    private final int priceCents;
    private final int stock;

    public Product(String name, int priceCents, int stock) {
        this.name = name;
        this.priceCents = priceCents;
        this.stock = stock;
    }

    public String getName() {
        return name;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getStock() {
        return stock;
    }

    @Override
    public String toString() {
        return name + "，价格 " + priceCents + " 分，库存 " + stock;
    }
}
