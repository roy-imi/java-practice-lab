package com.roy.juclab.answers;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public final class JucLab02AtomicInventoryAnswer {
    private final AtomicInteger remainingStock;
    private final LongAdder successfulOrders = new LongAdder();

    public JucLab02AtomicInventoryAnswer(int initialStock) {
        if (initialStock < 0) {
            throw new IllegalArgumentException("initialStock 不能小于 0");
        }
        this.remainingStock = new AtomicInteger(initialStock);
    }

    public boolean tryPurchase(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity 必须大于 0");
        }

        while (true) {
            int current = remainingStock.get();
            if (current < quantity) {
                return false;
            }
            if (remainingStock.compareAndSet(current, current - quantity)) {
                successfulOrders.increment();
                return true;
            }
        }
    }

    public int getRemainingStock() {
        return remainingStock.get();
    }

    public long getSuccessfulOrders() {
        return successfulOrders.sum();
    }
}
