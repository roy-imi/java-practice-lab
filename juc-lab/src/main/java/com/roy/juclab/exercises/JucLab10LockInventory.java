package com.roy.juclab.exercises;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Lab 10B：使用 ReentrantLock 和 Condition 实现同一库存需求。
 *
 * 课前文档：docs/guides/JUC_CONCURRENCY_GUIDE.md（Lab 10）
 * 练习重点：lockInterruptibly、awaitNanos、signalAll 和 finally unlock。
 */
public final class JucLab10LockInventory {
    private final ReentrantLock lock;
    private final Condition stockAvailable;
    private int stock;

    public JucLab10LockInventory(int initialStock, boolean fair) {
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
        /*
         * TODO：
         * - 校验参数，并把 timeout 转换成 nanos；
         * - 使用 lockInterruptibly 获取锁；
         * - 库存不足时在 while 中调用 stockAvailable.awaitNanos；
         * - 剩余时间 <= 0 时返回 false；
         * - 库存足够时扣减并返回 true；
         * - 无论如何都在 finally 中 unlock。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 10 的 ReentrantLock 库存");
    }

    public void restock(int quantity) {
        /*
         * TODO：加锁后增加库存，并使用 signalAll 通知等待线程。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 10 的 Condition 补货");
    }

    public int getRemainingStock() {
        /*
         * TODO：加锁读取库存，并在 finally 中释放锁。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 10 的 Lock 读取");
    }
}
