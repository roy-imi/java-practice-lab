package com.roy.juclab.answers;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class JucLab03PriceCacheAnswer {
    private final Map<String, Integer> prices = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public void put(String sku, int priceCents) {
        writeLock.lock();
        try {
            prices.put(sku, priceCents);
        } finally {
            writeLock.unlock();
        }
    }

    public OptionalInt get(String sku) {
        readLock.lock();
        try {
            Integer price = prices.get(sku);
            return price == null
                    ? OptionalInt.empty()
                    : OptionalInt.of(price);
        } finally {
            readLock.unlock();
        }
    }

    public Map<String, Integer> snapshot() {
        readLock.lock();
        try {
            return new HashMap<>(prices);
        } finally {
            readLock.unlock();
        }
    }
}
