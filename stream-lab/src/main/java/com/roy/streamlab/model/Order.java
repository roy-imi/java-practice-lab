package com.roy.streamlab.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Order {
    private final String id;
    private final String customer;
    private final String city;
    private final int amountCents;
    private final boolean paid;
    private final List<String> itemNames;

    public Order(String id,
                 String customer,
                 String city,
                 int amountCents,
                 boolean paid,
                 List<String> itemNames) {
        this.id = id;
        this.customer = customer;
        this.city = city;
        this.amountCents = amountCents;
        this.paid = paid;
        this.itemNames = Collections.unmodifiableList(
                new ArrayList<>(itemNames));
    }

    public String getId() {
        return id;
    }

    public String getCustomer() {
        return customer;
    }

    public String getCity() {
        return city;
    }

    public int getAmountCents() {
        return amountCents;
    }

    public boolean isPaid() {
        return paid;
    }

    public List<String> getItemNames() {
        return itemNames;
    }

    @Override
    public String toString() {
        return id + "，客户 " + customer
                + "，城市 " + city
                + "，金额 " + amountCents + " 分"
                + "，" + (paid ? "已支付" : "未支付");
    }
}
