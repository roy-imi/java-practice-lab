package com.roy.juclab;

import com.roy.juclab.exercises.JucLab01ThreadLifecycle;
import com.roy.juclab.exercises.JucLab02AtomicInventory;
import com.roy.juclab.exercises.JucLab03PriceCache;
import com.roy.juclab.exercises.JucLab04RequestStatistics;
import com.roy.juclab.exercises.JucLab05ConcurrencyGate;
import com.roy.juclab.exercises.JucLab06BlockingQueuePipeline;
import com.roy.juclab.exercises.JucLab07ThreadPoolFactory;
import com.roy.juclab.exercises.JucLab08FlashSaleService;
import com.roy.juclab.model.PurchaseResult;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntConsumer;

public final class JucExerciseCheck {
    private JucExerciseCheck() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("用法：JucExerciseCheck <1-8>");
        }

        int lesson = Integer.parseInt(args[0]);
        try {
            check(lesson);
            System.out.println("[通过] JUC 第 " + lesson + " 课符合预期。");
        } catch (UnsupportedOperationException todo) {
            System.out.println("[未完成] " + todo.getMessage());
            System.exit(1);
        } catch (AssertionError wrong) {
            System.out.println("[未通过] " + wrong.getMessage());
            System.exit(1);
        } catch (Exception error) {
            System.out.println("[运行失败] " + error);
            System.exit(1);
        }
    }

    private static void check(int lesson) throws Exception {
        switch (lesson) {
            case 1:
                checkLab01();
                return;
            case 2:
                checkLab02();
                return;
            case 3:
                checkLab03();
                return;
            case 4:
                checkLab04();
                return;
            case 5:
                checkLab05();
                return;
            case 6:
                checkLab06();
                return;
            case 7:
                checkLab07();
                return;
            case 8:
                checkLab08();
                return;
            default:
                throw new IllegalArgumentException("课次只能是 1 到 8");
        }
    }

    private static void checkLab01() throws InterruptedException {
        AtomicReference<String> threadName = new AtomicReference<>();
        Thread worker = JucLab01ThreadLifecycle.startTask(
                "order-worker-1",
                () -> threadName.set(Thread.currentThread().getName()));
        JucLab01ThreadLifecycle.waitFor(worker);

        assertEquals(
                "order-worker-1",
                threadName.get(),
                "任务没有在指定名称的工作线程中执行");
        assertTrue(!worker.isAlive(), "waitFor 返回后线程应该已经结束");

        CountDownLatch started = new CountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean();
        Thread sleeper = JucLab01ThreadLifecycle.startTask(
                "interrupt-worker",
                () -> {
                    started.countDown();
                    try {
                        Thread.sleep(10_000);
                    } catch (InterruptedException expected) {
                        interrupted.set(true);
                    }
                });
        started.await();
        JucLab01ThreadLifecycle.requestStop(sleeper);
        JucLab01ThreadLifecycle.waitFor(sleeper);
        assertTrue(interrupted.get(), "工作线程没有收到中断请求");
    }

    private static void checkLab02() throws Exception {
        JucLab02AtomicInventory inventory =
                new JucLab02AtomicInventory(30);
        AtomicInteger successes = new AtomicInteger();

        runConcurrently(80, index -> {
            if (inventory.tryPurchase(1)) {
                successes.incrementAndGet();
            }
        });

        assertEquals(30, successes.get(), "成功购买数量错误");
        assertEquals(0, inventory.getRemainingStock(), "库存不应小于 0");
        assertEquals(
                30L,
                inventory.getSuccessfulOrders(),
                "成功订单计数错误");

        assertThrowsIllegalArgument(
                () -> inventory.tryPurchase(0),
                "quantity 为 0 时应该拒绝");
    }

    private static void checkLab03() throws Exception {
        JucLab03PriceCache cache = new JucLab03PriceCache();
        cache.put("SKU-1", 19_900);
        assertEquals(
                OptionalInt.of(19_900),
                cache.get("SKU-1"),
                "价格读取错误");
        assertEquals(
                OptionalInt.empty(),
                cache.get("missing"),
                "不存在的 SKU 应返回 OptionalInt.empty()");

        runConcurrently(20, index ->
                cache.put("SKU-" + index, index * 100));
        Map<String, Integer> snapshot = cache.snapshot();
        assertEquals(20, snapshot.size(), "并发写入后的缓存数量错误");

        snapshot.put("OUTSIDE", 1);
        assertEquals(
                OptionalInt.empty(),
                cache.get("OUTSIDE"),
                "snapshot 不应暴露内部 Map");
    }

    private static void checkLab04() throws Exception {
        JucLab04RequestStatistics statistics =
                new JucLab04RequestStatistics();

        runConcurrently(16, index -> {
            for (int count = 0; count < 1_000; count++) {
                statistics.record("/orders");
            }
            statistics.record("/worker/" + index);
        });

        assertEquals(
                16_000L,
                statistics.count("/orders"),
                "高竞争请求计数丢失");
        assertEquals(
                0L,
                statistics.count("/missing"),
                "不存在的接口应该返回 0");
        assertEquals(
                17,
                statistics.snapshot().size(),
                "统计快照的 key 数量错误");
    }

    private static void checkLab05() throws Exception {
        int taskCount = 12;
        int permitCount = 3;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch workersReady = new CountDownLatch(taskCount);
        Semaphore semaphore = new Semaphore(permitCount);
        AtomicInteger active = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        AtomicInteger completed = new AtomicInteger();
        Thread[] workers = new Thread[taskCount];

        Runnable limited = JucLab05ConcurrencyGate.limitConcurrency(
                semaphore,
                () -> {
                    int current = active.incrementAndGet();
                    updateMaximum(peak, current);
                    try {
                        Thread.sleep(15);
                        completed.incrementAndGet();
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                    } finally {
                        active.decrementAndGet();
                    }
                });

        for (int index = 0; index < taskCount; index++) {
            Runnable waiting =
                    JucLab05ConcurrencyGate.waitForStart(startSignal, limited);
            workers[index] = new Thread(() -> {
                workersReady.countDown();
                waiting.run();
            }, "gate-check-" + index);
            workers[index].start();
        }

        workersReady.await();
        assertEquals(0, completed.get(), "放行前不应执行任务");
        startSignal.countDown();
        for (Thread worker : workers) {
            worker.join();
        }

        assertEquals(taskCount, completed.get(), "存在未完成任务");
        assertTrue(
                peak.get() <= permitCount,
                "同时执行数超过 Semaphore 许可数");
        assertEquals(
                permitCount,
                semaphore.availablePermits(),
                "许可没有被完整归还");
    }

    private static void checkLab06() throws InterruptedException {
        List<String> result = JucLab06BlockingQueuePipeline.process(
                Arrays.asList("order-a", "order-b", "order-c", "order-d"),
                3,
                value -> value.toUpperCase());

        assertEquals(
                Arrays.asList("ORDER-A", "ORDER-B", "ORDER-C", "ORDER-D"),
                result,
                "并发处理结果或输入顺序错误");
    }

    private static void checkLab07() throws InterruptedException {
        ThreadPoolExecutor executor =
                JucLab07ThreadPoolFactory.create(1, 1, 1, "checkout");
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();

        try {
            executor.execute(() -> {
                threadName.set(Thread.currentThread().getName());
                firstStarted.countDown();
                try {
                    releaseFirst.await();
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            });
            firstStarted.await();
            executor.execute(() -> {
            });

            boolean rejected = false;
            try {
                executor.execute(() -> {
                });
            } catch (RejectedExecutionException expected) {
                rejected = true;
            }

            assertTrue(rejected, "线程和队列都满时应使用 AbortPolicy 拒绝");
            assertEquals(1, executor.getCorePoolSize(), "核心线程数错误");
            assertEquals(1, executor.getMaximumPoolSize(), "最大线程数错误");
            assertEquals(
                    1,
                    executor.getQueue().size(),
                    "有界队列容量或执行流程错误");
            assertEquals(
                    "checkout-1",
                    threadName.get(),
                    "线程名称不符合 prefix-序号");
        } finally {
            releaseFirst.countDown();
        }

        assertTrue(
                JucLab07ThreadPoolFactory.shutdownGracefully(
                        executor, 2, TimeUnit.SECONDS),
                "线程池未能优雅关闭");
        assertTrue(executor.isTerminated(), "线程池应该已经终止");
    }

    private static void checkLab08() throws Exception {
        JucLab08FlashSaleService service =
                new JucLab08FlashSaleService(25);
        AtomicInteger successfulCalls = new AtomicInteger();

        runConcurrently(100, index -> {
            PurchaseResult result =
                    service.purchase("request-" + index);
            if (result.isSuccess()) {
                successfulCalls.incrementAndGet();
            }
        });

        assertEquals(25, successfulCalls.get(), "成功数不等于初始库存");
        assertEquals(0, service.getRemainingStock(), "发生超卖或库存未扣完");
        assertEquals(
                100,
                service.getRecordedRequestCount(),
                "不同 requestId 没有完整记录");

        PurchaseResult first = service.purchase("request-0");
        int stockBeforeRetry = service.getRemainingStock();
        PurchaseResult retry = service.purchase("request-0");
        assertTrue(first == retry, "相同 requestId 应返回已记录的同一结果");
        assertEquals(
                stockBeforeRetry,
                service.getRemainingStock(),
                "重复请求不应再次扣减库存");
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
            }, "concurrency-check-" + index);
            thread.start();
        }

        ready.await();
        start.countDown();
        done.await();
        rethrow(failure.get());
    }

    private static void rethrow(Throwable error) throws Exception {
        if (error == null) {
            return;
        }
        if (error instanceof UnsupportedOperationException) {
            throw (UnsupportedOperationException) error;
        }
        if (error instanceof AssertionError) {
            throw (AssertionError) error;
        }
        if (error instanceof Exception) {
            throw (Exception) error;
        }
        throw new RuntimeException(error);
    }

    private static void updateMaximum(AtomicInteger maximum, int value) {
        while (true) {
            int current = maximum.get();
            if (value <= current
                    || maximum.compareAndSet(current, value)) {
                return;
            }
        }
    }

    private static void assertThrowsIllegalArgument(
            Runnable action,
            String message) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(message);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected,
                                     Object actual,
                                     String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(
                    message + "；期望 " + expected + "，实际 " + actual);
        }
    }
}
