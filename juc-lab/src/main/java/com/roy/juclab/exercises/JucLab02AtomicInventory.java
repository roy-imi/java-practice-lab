package com.roy.juclab.exercises;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/*
 * Lab 02：使用 CAS 实现不会超卖的单机库存。
 *
 * 课前文档：docs/guides/JUC_CONCURRENCY_GUIDE.md（Lab 02）
 * 练习重点：AtomicInteger、compareAndSet 重试循环和 LongAdder。
 */
public final class JucLab02AtomicInventory {
    private final AtomicInteger remainingStock;
    private final LongAdder successfulOrders = new LongAdder();

    public JucLab02AtomicInventory(int initialStock) {
        if (initialStock < 0) {
            throw new IllegalArgumentException("initialStock 不能小于 0");
        }
        this.remainingStock = new AtomicInteger(initialStock);
    }

    public boolean tryPurchase(int quantity) {
        /*
         * TODO：
         * - quantity 必须大于 0；
         * - 读取当前库存，不足时返回 false；
         * - 使用 compareAndSet 扣减，失败时重新读取并重试；
         * - 成功后将 successfulOrders 加一。
         */
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity 必须大于0");
        }

        while (true) {
            int curStock = remainingStock.get();
            if (curStock < quantity) {
                return false;
            }
            boolean flag = remainingStock.compareAndSet(curStock, curStock - quantity);
            if (flag) {
                successfulOrders.increment();
                return true;
            }
        }
    }

    public int getRemainingStock() {
        /*
         * TODO：原子读取剩余库存。
         */
        return remainingStock.get();
    }

    public long getSuccessfulOrders() {
        /*
         * TODO：读取成功订单次数。注意这不是售出的商品件数。
         */
        return successfulOrders.sum();
    }
}
