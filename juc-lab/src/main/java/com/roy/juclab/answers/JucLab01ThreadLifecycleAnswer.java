package com.roy.juclab.answers;

import java.util.Objects;

public final class JucLab01ThreadLifecycleAnswer {
    private JucLab01ThreadLifecycleAnswer() {
    }

    public static Thread startTask(String threadName, Runnable task) {
        Objects.requireNonNull(threadName, "threadName");
        Objects.requireNonNull(task, "task");

        Thread thread = new Thread(task, threadName);
        thread.start();
        return thread;
    }

    public static void waitFor(Thread thread) throws InterruptedException {
        Objects.requireNonNull(thread, "thread");
        thread.join();
    }

    public static void requestStop(Thread thread) {
        Objects.requireNonNull(thread, "thread");
        thread.interrupt();
    }
}
