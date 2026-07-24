package com.roy.juclab.exercises;

import java.util.concurrent.TimeUnit;

/*
 * Lab 10A：使用 synchronized、wait 和 notifyAll 管理库存条件。
 *
 * 课前文档：docs/guides/JUC_CONCURRENCY_GUIDE.md（Lab 10）
 * 练习重点：私有监视器、同步代码块、while 检查条件、超时和异常释放锁。
 */
public final class JucLab10SynchronizedInventory {
    private final Object monitor = new Object();
    private int stock;

    public JucLab10SynchronizedInventory(int initialStock) {
        if (initialStock < 0) {
            throw new IllegalArgumentException("initialStock 不能小于 0");
        }
        this.stock = initialStock;
    }

    public boolean awaitAndPurchase(
            int quantity,
            long timeout,
            TimeUnit unit) throws InterruptedException {
        /*
         * TODO：
         * - 校验 quantity > 0、timeout >= 0、unit 不为 null；
         * - 使用 System.nanoTime 计算剩余等待时间；
         * - synchronized(monitor) 保护检查和扣减；
         * - 库存不足时在 while 中调用 monitor.wait(millis, nanos)；
         * - 超时返回 false，库存足够时扣减并返回 true。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 10 的 synchronized 库存");
    }

    public void restock(int quantity) {
        /*
         * TODO：
         * - quantity 必须大于 0；
         * - 在 synchronized(monitor) 中增加库存；
         * - 使用 notifyAll 唤醒所有等待者重新检查条件。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 10 的 synchronized 补货");
    }

    public int getRemainingStock() {
        /*
         * TODO：使用同一个 monitor 保护库存读取。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 10 的 synchronized 读取");
    }
}
