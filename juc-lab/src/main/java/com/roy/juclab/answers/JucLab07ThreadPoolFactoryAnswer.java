package com.roy.juclab.answers;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class JucLab07ThreadPoolFactoryAnswer {
    private JucLab07ThreadPoolFactoryAnswer() {
    }

    public static ThreadPoolExecutor create(
            int corePoolSize,
            int maximumPoolSize,
            int queueCapacity,
            String threadNamePrefix) {
        AtomicInteger sequence = new AtomicInteger();
        ThreadFactory threadFactory = task -> new Thread(
                task,
                threadNamePrefix + "-" + sequence.incrementAndGet());

        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                30L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
    }

    public static boolean shutdownGracefully(
            ThreadPoolExecutor executor,
            long timeout,
            TimeUnit unit) {
        executor.shutdown();
        try {
            if (executor.awaitTermination(timeout, unit)) {
                return true;
            }

            executor.shutdownNow();
            return executor.awaitTermination(timeout, unit);
        } catch (InterruptedException interrupted) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
