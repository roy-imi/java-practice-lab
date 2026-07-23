package com.roy.juclab;

import com.roy.juclab.answers.JucLab01ThreadLifecycleAnswer;
import com.roy.juclab.answers.JucLab02AtomicInventoryAnswer;
import com.roy.juclab.answers.JucLab03PriceCacheAnswer;
import com.roy.juclab.answers.JucLab04RequestStatisticsAnswer;
import com.roy.juclab.answers.JucLab05ConcurrencyGateAnswer;
import com.roy.juclab.answers.JucLab06BlockingQueuePipelineAnswer;
import com.roy.juclab.answers.JucLab07ThreadPoolFactoryAnswer;
import com.roy.juclab.answers.JucLab08FlashSaleServiceAnswer;
import com.roy.juclab.model.PurchaseResult;

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntConsumer;

public final class JucSelfCheck {
    private JucSelfCheck() {
    }

    public static void main(String[] args) throws Exception {
        checkLab01();
        checkLab02();
        checkLab03();
        checkLab04();
        checkLab05();
        checkLab06();
        checkLab07();
        checkLab08();
        System.out.println();
        System.out.println("全部通过：JUC 8/8。");
    }

    private static void checkLab01() throws InterruptedException {
        AtomicReference<String> name = new AtomicReference<>();
        Thread thread = JucLab01ThreadLifecycleAnswer.startTask(
                "answer-worker",
                () -> name.set(Thread.currentThread().getName()));
        JucLab01ThreadLifecycleAnswer.waitFor(thread);
        assertEquals("answer-worker", name.get());
        pass(1, "线程生命周期与中断");
    }

    private static void checkLab02() throws Exception {
        JucLab02AtomicInventoryAnswer inventory =
                new JucLab02AtomicInventoryAnswer(20);
        AtomicInteger successful = new AtomicInteger();
        runConcurrently(50, index -> {
            if (inventory.tryPurchase(1)) {
                successful.incrementAndGet();
            }
        });
        assertEquals(20, successful.get());
        assertEquals(0, inventory.getRemainingStock());
        assertEquals(20L, inventory.getSuccessfulOrders());
        pass(2, "AtomicInteger 与 LongAdder");
    }

    private static void checkLab03() throws Exception {
        JucLab03PriceCacheAnswer cache =
                new JucLab03PriceCacheAnswer();
        runConcurrently(12, index ->
                cache.put("SKU-" + index, index * 100));
        assertEquals(12, cache.snapshot().size());
        assertEquals(OptionalInt.of(500), cache.get("SKU-5"));
        pass(3, "读写锁缓存");
    }

    private static void checkLab04() throws Exception {
        JucLab04RequestStatisticsAnswer statistics =
                new JucLab04RequestStatisticsAnswer();
        runConcurrently(10, index -> {
            for (int count = 0; count < 500; count++) {
                statistics.record("/orders");
            }
        });
        assertEquals(5_000L, statistics.count("/orders"));
        pass(4, "ConcurrentHashMap 并发统计");
    }

    private static void checkLab05() throws InterruptedException {
        CountDownLatch start = new CountDownLatch(1);
        Semaphore semaphore = new Semaphore(2);
        AtomicInteger completed = new AtomicInteger();
        Thread[] workers = new Thread[6];

        for (int index = 0; index < workers.length; index++) {
            Runnable task =
                    JucLab05ConcurrencyGateAnswer.waitForStart(
                            start,
                            JucLab05ConcurrencyGateAnswer.limitConcurrency(
                                    semaphore,
                                    completed::incrementAndGet));
            workers[index] = new Thread(task);
            workers[index].start();
        }
        assertEquals(0, completed.get());
        start.countDown();
        for (Thread worker : workers) {
            worker.join();
        }
        assertEquals(6, completed.get());
        assertEquals(2, semaphore.availablePermits());
        pass(5, "CountDownLatch 与 Semaphore");
    }

    private static void checkLab06() throws InterruptedException {
        assertEquals(
                Arrays.asList("A!", "B!", "C!"),
                JucLab06BlockingQueuePipelineAnswer.process(
                        Arrays.asList("A", "B", "C"),
                        2,
                        value -> value + "!"));
        pass(6, "BlockingQueue 生产者消费者");
    }

    private static void checkLab07() throws InterruptedException {
        ThreadPoolExecutor executor =
                JucLab07ThreadPoolFactoryAnswer.create(
                        1, 2, 2, "payment");
        CountDownLatch ran = new CountDownLatch(1);
        AtomicReference<String> name = new AtomicReference<>();
        executor.execute(() -> {
            name.set(Thread.currentThread().getName());
            ran.countDown();
        });
        ran.await();
        assertEquals("payment-1", name.get());
        assertTrue(
                JucLab07ThreadPoolFactoryAnswer.shutdownGracefully(
                        executor, 1, TimeUnit.SECONDS));
        pass(7, "有界 ThreadPoolExecutor");
    }

    private static void checkLab08() throws Exception {
        JucLab08FlashSaleServiceAnswer service =
                new JucLab08FlashSaleServiceAnswer(15);
        AtomicInteger successful = new AtomicInteger();
        runConcurrently(60, index -> {
            PurchaseResult result =
                    service.purchase("answer-request-" + index);
            if (result.isSuccess()) {
                successful.incrementAndGet();
            }
        });
        assertEquals(15, successful.get());
        assertEquals(0, service.getRemainingStock());
        assertEquals(60, service.getRecordedRequestCount());

        PurchaseResult first = service.purchase("answer-request-0");
        PurchaseResult retry = service.purchase("answer-request-0");
        assertTrue(first == retry);
        pass(8, "秒杀防超卖与幂等");
    }

    private static void runConcurrently(int threadCount,
                                        IntConsumer action)
            throws Exception {
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        for (int index = 0; index < threadCount; index++) {
            final int taskIndex = index;
            Thread thread = new Thread(() -> {
                ready.countDown();
                try {
                    start.await();
                    action.accept(taskIndex);
                } catch (Throwable error) {
                    failure.compareAndSet(null, error);
                } finally {
                    done.countDown();
                }
            });
            thread.start();
        }

        ready.await();
        start.countDown();
        done.await();
        Throwable error = failure.get();
        if (error != null) {
            throw new RuntimeException(error);
        }
    }

    private static void pass(int lesson, String topic) {
        System.out.println("[通过] 第 " + lesson + " 课：" + topic);
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("条件不成立");
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(
                    "期望 " + expected + "，实际 " + actual);
        }
    }
}
