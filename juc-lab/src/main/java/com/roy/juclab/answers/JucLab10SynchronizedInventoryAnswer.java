package com.roy.juclab.answers;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class JucLab10SynchronizedInventoryAnswer {
    private final Object monitor = new Object();
    private int stock;

    public JucLab10SynchronizedInventoryAnswer(int initialStock) {
        if (initialStock < 0) {
            throw new IllegalArgumentException("initialStock 不能小于 0");
        }
        this.stock = initialStock;
    }

    public boolean awaitAndPurchase(
            int quantity,
            long timeout,
            TimeUnit unit) throws InterruptedException {
        validate(quantity, timeout, unit);
        long remainingNanos = unit.toNanos(timeout);
        long deadline = System.nanoTime() + remainingNanos;

        synchronized (monitor) {
            while (stock < quantity) {
                if (remainingNanos <= 0L) {
                    return false;
                }

                long millis =
                        TimeUnit.NANOSECONDS.toMillis(remainingNanos);
                int nanos = (int) (
                        remainingNanos
                                - TimeUnit.MILLISECONDS.toNanos(millis));
                monitor.wait(millis, nanos);
                remainingNanos = deadline - System.nanoTime();
            }

            stock -= quantity;
            return true;
        }
    }

    public void restock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity 必须大于 0");
        }
        synchronized (monitor) {
            stock += quantity;
            monitor.notifyAll();
        }
    }

    public int getRemainingStock() {
        synchronized (monitor) {
            return stock;
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
