package com.roy.juclab.answers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public final class JucLab05ConcurrencyGateAnswer {
    private JucLab05ConcurrencyGateAnswer() {
    }

    public static Runnable waitForStart(CountDownLatch startSignal,
                                        Runnable task) {
        return () -> {
            try {
                startSignal.await();
                task.run();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        };
    }

    public static Runnable limitConcurrency(Semaphore permits,
                                            Runnable task) {
        return () -> {
            boolean acquired = false;
            try {
                permits.acquire();
                acquired = true;
                task.run();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            } finally {
                if (acquired) {
                    permits.release();
                }
            }
        };
    }
}
