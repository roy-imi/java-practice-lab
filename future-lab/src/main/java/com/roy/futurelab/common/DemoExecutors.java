package com.roy.futurelab.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class DemoExecutors {
    private DemoExecutors() {
    }

    public static ExecutorService newPool(String lessonName, int size) {
        return Executors.newFixedThreadPool(size, new NamedThreadFactory(lessonName));
    }

    public static void shutdown(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException interrupted) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
