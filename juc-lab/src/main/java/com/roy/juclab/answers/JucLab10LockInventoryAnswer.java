package com.roy.juclab.answers;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class JucLab10LockInventoryAnswer {
    private final ReentrantLock lock;
    private final Condition stockAvailable;
    private int stock;

    public JucLab10LockInventoryAnswer(int initialStock, boolean fair) {
        if (initialStock < 0) {
            throw new IllegalArgumentException("initialStock 不能小于 0");
        }
        this.stock = initialStock;
        this.lock = new ReentrantLock(fair);
        this.stockAvailable = lock.newCondition();
    }

    public boolean awaitAndPurchase(
            int quantity,
            long timeout,
            TimeUnit unit) throws InterruptedException {
        validate(quantity, timeout, unit);
        long remainingNanos = unit.toNanos(timeout);

        lock.lockInterruptibly();
        try {
            while (stock < quantity) {
                if (remainingNanos <= 0L) {
                    return false;
                }
                remainingNanos =
                        stockAvailable.awaitNanos(remainingNanos);
            }

            stock -= quantity;
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void restock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity 必须大于 0");
        }

        lock.lock();
        try {
            stock += quantity;
            stockAvailable.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public int getRemainingStock() {
        lock.lock();
        try {
            return stock;
        } finally {
            lock.unlock();
        }
    }

    private static void validate(int quantity,
                                 long timeout,
                                 TimeUnit unit) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity 必须大于 0");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout 不能小于 0");
        }
        Objects.requireNonNull(unit, "unit");
    }
}
