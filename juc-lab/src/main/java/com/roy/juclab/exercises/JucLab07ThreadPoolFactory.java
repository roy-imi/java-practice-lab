package com.roy.juclab.exercises;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
 * Lab 07：创建参数明确、有界、可观测、可关闭的线程池。
 *
 * 课前文档：docs/guides/JUC_CONCURRENCY_GUIDE.md（Lab 07）
 * 练习重点：ThreadPoolExecutor 参数、有界队列、线程命名、拒绝策略和关闭。
 */
public final class JucLab07ThreadPoolFactory {
    private JucLab07ThreadPoolFactory() {
    }

    public static ThreadPoolExecutor create(
            int corePoolSize,
            int maximumPoolSize,
            int queueCapacity,
            String threadNamePrefix) {
        /*
         * TODO：
         * - keepAliveTime 使用 30 秒；
         * - 工作队列使用指定容量的 ArrayBlockingQueue；
         * - 自定义 ThreadFactory，线程名为 prefix-1、prefix-2……
         * - 拒绝策略使用 AbortPolicy，过载时明确抛出异常。
         */
        throw new UnsupportedOperationException("TODO: 完成 JUC Lab 07 的线程池创建");
    }

    public static boolean shutdownGracefully(
            ThreadPoolExecutor executor,
            long timeout,
            TimeUnit unit) {
        /*
         * TODO：
         * - 先 shutdown，停止接收新任务；
         * - 在超时时间内等待已有任务完成；
         * - 超时后 shutdownNow，再等待一次；
         * - 当前线程被中断时 shutdownNow、恢复中断标记并返回 false。
         */
        throw new UnsupportedOperationException("TODO: 完成 JUC Lab 07 的优雅关闭");
    }
}
