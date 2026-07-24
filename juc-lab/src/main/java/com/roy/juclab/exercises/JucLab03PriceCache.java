package com.roy.juclab.exercises;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
 * Lab 03：用读写锁保护“读多写少”的商品价格缓存。
 *
 * 课前文档：docs/guides/JUC_CONCURRENCY_GUIDE.md（Lab 03）
 * 练习重点：readLock、writeLock，以及必须放在 finally 中的 unlock。
 */
public final class JucLab03PriceCache {
    private final Map<String, Integer> prices = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public void put(String sku, int priceCents) {
        /*
         * TODO：使用写锁更新价格，并保证任何异常下都能释放锁。
         */
        writeLock.lock();
        try {
            prices.put(sku, priceCents);
        } finally {
            writeLock.unlock();
        }
    }

    public OptionalInt get(String sku) {
        /*
         * TODO：使用读锁查询；存在时返回 OptionalInt.of，否则 empty。
         */
        readLock.lock();
        try {
            Integer priceCent = prices.get(sku);
            return priceCent == null ?
                    OptionalInt.empty() : OptionalInt.of(priceCent);
        } finally {
            readLock.unlock();
        }
    }

    public Map<String, Integer> snapshot() {
        /*
         * TODO：在读锁保护下返回一个新的 HashMap，不能暴露内部可变 Map。
         */
        readLock.lock();
        try {
            return new HashMap<String, Integer>(prices);
        } finally {
            readLock.unlock();
        }
    }
}
