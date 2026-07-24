package com.roy.juclab;

import com.roy.juclab.exercises.JucLab01ThreadLifecycle;
import com.roy.juclab.exercises.JucLab02AtomicInventory;
import com.roy.juclab.exercises.JucLab03PriceCache;
import com.roy.juclab.exercises.JucLab04RequestStatistics;
import com.roy.juclab.exercises.JucLab05ConcurrencyGate;
import com.roy.juclab.exercises.JucLab06BlockingQueuePipeline;
import com.roy.juclab.exercises.JucLab07ThreadPoolFactory;
import com.roy.juclab.exercises.JucLab08FlashSaleService;
import com.roy.juclab.exercises.JucLab09ThreadLocalContext;
import com.roy.juclab.exercises.JucLab10LockInventory;
import com.roy.juclab.exercises.JucLab10SynchronizedInventory;
import com.roy.juclab.exercises.JucLab11VolatileServiceState;
import com.roy.juclab.model.PurchaseResult;
import com.roy.juclab.model.RequestContext;
import com.roy.juclab.model.ServiceConfig;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
            throw new IllegalArgumentException("用法：JucExerciseCheck <1-11>");
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
            case 9:
                checkLab09();
                return;
            case 10:
                checkLab10();
                return;
            case 11:
                checkLab11();
                return;
            default:
                throw new IllegalArgumentException("课次只能是 1 到 11");
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

    private static void checkLab09() throws Exception {
        RequestContext caller =
                new RequestContext("request-1001", "user-42");
        JucLab09ThreadLocalContext.clear();
        assertTrue(
                !JucLab09ThreadLocalContext.current().isPresent(),
                "clear 后当前线程应该没有上下文");

        JucLab09ThreadLocalContext.set(caller);
        assertEquals(
                caller,
                JucLab09ThreadLocalContext.current().orElse(null),
                "set 后无法在当前线程读取上下文");

        ExecutorService executor =
                Executors.newSingleThreadExecutor();
        try {
            RequestContext unpropagated = executor.submit(
                    () -> JucLab09ThreadLocalContext
                            .current()
                            .orElse(null))
                    .get(2, TimeUnit.SECONDS);
            assertEquals(
                    null,
                    unpropagated,
                    "普通 ThreadLocal 不应自动出现在另一个线程");

            AtomicReference<RequestContext> observed =
                    new AtomicReference<>();
            Runnable contextAware =
                    JucLab09ThreadLocalContext.wrap(
                            () -> observed.set(
                                    JucLab09ThreadLocalContext
                                            .current()
                                            .orElse(null)));
            JucLab09ThreadLocalContext.clear();

            executor.submit(contextAware)
                    .get(2, TimeUnit.SECONDS);
            assertEquals(
                    caller,
                    observed.get(),
                    "包装任务没有读取到提交线程捕获的上下文");

            RequestContext leaked = executor.submit(
                    () -> JucLab09ThreadLocalContext
                            .current()
                            .orElse(null))
                    .get(2, TimeUnit.SECONDS);
            assertEquals(
                    null,
                    leaked,
                    "任务结束后上下文残留在线程池工作线程");
        } finally {
            executor.shutdownNow();
            JucLab09ThreadLocalContext.clear();
        }

        RequestContext captured =
                new RequestContext("request-inner", "user-inner");
        RequestContext previous =
                new RequestContext("request-outer", "user-outer");
        AtomicReference<RequestContext> inside =
                new AtomicReference<>();

        JucLab09ThreadLocalContext.set(captured);
        Runnable failing = JucLab09ThreadLocalContext.wrap(() -> {
            inside.set(
                    JucLab09ThreadLocalContext
                            .current()
                            .orElse(null));
            throw new IllegalStateException("模拟业务异常");
        });
        JucLab09ThreadLocalContext.set(previous);

        boolean failed = false;
        try {
            failing.run();
        } catch (IllegalStateException expected) {
            failed = true;
        }

        assertTrue(failed, "包装器不应吞掉业务异常");
        assertEquals(
                captured,
                inside.get(),
                "任务执行期间没有安装捕获的上下文");
        assertEquals(
                previous,
                JucLab09ThreadLocalContext.current().orElse(null),
                "任务异常结束后没有恢复原上下文");
        JucLab09ThreadLocalContext.clear();
    }

    private static void checkLab10() throws Exception {
        checkSynchronizedInventory();
        checkLockInventory();
    }

    private static void checkSynchronizedInventory() throws Exception {
        JucLab10SynchronizedInventory waitingInventory =
                new JucLab10SynchronizedInventory(0);
        AtomicBoolean purchased = new AtomicBoolean();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch started = new CountDownLatch(1);

        Thread buyer = new Thread(() -> {
            started.countDown();
            try {
                purchased.set(
                        waitingInventory.awaitAndPurchase(
                                2, 2, TimeUnit.SECONDS));
            } catch (Throwable error) {
                failure.set(error);
            }
        }, "synchronized-buyer");
        buyer.start();
        started.await();
        waitingInventory.restock(2);
        buyer.join(2_500);

        assertTrue(!buyer.isAlive(), "synchronized 等待线程没有结束");
        rethrow(failure.get());
        assertTrue(purchased.get(), "补货后 synchronized 购买应该成功");
        assertEquals(
                0,
                waitingInventory.getRemainingStock(),
                "synchronized 扣减后的库存错误");
        long synchronizedStart = System.nanoTime();
        boolean synchronizedTimedOut =
                !waitingInventory.awaitAndPurchase(
                        1, 80, TimeUnit.MILLISECONDS);
        long synchronizedElapsed =
                TimeUnit.NANOSECONDS.toMillis(
                        System.nanoTime() - synchronizedStart);
        assertTrue(
                synchronizedTimedOut,
                "synchronized 库存不足时应该超时返回 false");
        assertTrue(
                synchronizedElapsed >= 40,
                "synchronized 没有按超时预算等待");

        JucLab10SynchronizedInventory concurrentInventory =
                new JucLab10SynchronizedInventory(30);
        AtomicInteger successes = new AtomicInteger();
        runConcurrently(80, index -> {
            if (purchaseImmediately(concurrentInventory)) {
                successes.incrementAndGet();
            }
        });
        assertEquals(30, successes.get(), "synchronized 并发购买数量错误");
        assertEquals(
                0,
                concurrentInventory.getRemainingStock(),
                "synchronized 发生超卖");
    }

    private static void checkLockInventory() throws Exception {
        JucLab10LockInventory waitingInventory =
                new JucLab10LockInventory(0, true);
        AtomicBoolean purchased = new AtomicBoolean();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch started = new CountDownLatch(1);

        Thread buyer = new Thread(() -> {
            started.countDown();
            try {
                purchased.set(
                        waitingInventory.awaitAndPurchase(
                                2, 2, TimeUnit.SECONDS));
            } catch (Throwable error) {
                failure.set(error);
            }
        }, "lock-buyer");
        buyer.start();
        started.await();
        waitingInventory.restock(2);
        buyer.join(2_500);

        assertTrue(!buyer.isAlive(), "Condition 等待线程没有结束");
        rethrow(failure.get());
        assertTrue(purchased.get(), "signalAll 后 Lock 购买应该成功");
        assertEquals(
                0,
                waitingInventory.getRemainingStock(),
                "ReentrantLock 扣减后的库存错误");
        long lockStart = System.nanoTime();
        boolean lockTimedOut =
                !waitingInventory.awaitAndPurchase(
                        1, 80, TimeUnit.MILLISECONDS);
        long lockElapsed =
                TimeUnit.NANOSECONDS.toMillis(
                        System.nanoTime() - lockStart);
        assertTrue(
                lockTimedOut,
                "Condition 库存不足时应该超时返回 false");
        assertTrue(
                lockElapsed >= 40,
                "Condition 没有按超时预算等待");

        JucLab10LockInventory interruptibleInventory =
                new JucLab10LockInventory(0, false);
        AtomicBoolean interrupted = new AtomicBoolean();
        Thread interruptibleBuyer = new Thread(() -> {
            try {
                interruptibleInventory.awaitAndPurchase(
                        1, 5, TimeUnit.SECONDS);
            } catch (InterruptedException expected) {
                interrupted.set(true);
            }
        }, "interruptible-lock-buyer");
        interruptibleBuyer.start();
        awaitState(
                interruptibleBuyer,
                Thread.State.TIMED_WAITING,
                1,
                TimeUnit.SECONDS);
        interruptibleBuyer.interrupt();
        interruptibleBuyer.join(1_000);
        assertTrue(
                interrupted.get(),
                "Condition 等待没有响应 interrupt");
        assertTrue(
                !interruptibleBuyer.isAlive(),
                "中断后 Condition 等待线程没有结束");

        JucLab10LockInventory concurrentInventory =
                new JucLab10LockInventory(30, false);
        AtomicInteger successes = new AtomicInteger();
        runConcurrently(80, index -> {
            if (purchaseImmediately(concurrentInventory)) {
                successes.incrementAndGet();
            }
        });
        assertEquals(30, successes.get(), "ReentrantLock 并发购买数量错误");
        assertEquals(
                0,
                concurrentInventory.getRemainingStock(),
                "ReentrantLock 发生超卖");
    }

    private static void checkLab11() throws Exception {
        ServiceConfig initial =
                new ServiceConfig(
                        1,
                        "https://api-v1.example",
                        1_000);
        JucLab11VolatileServiceState state =
                new JucLab11VolatileServiceState(initial);

        assertEquals(
                initial,
                state.currentConfig(),
                "初始配置读取错误");
        assertTrue(
                Modifier.isVolatile(
                        JucLab11VolatileServiceState.class
                                .getDeclaredField("running")
                                .getModifiers()),
                "running 字段必须使用 volatile");
        assertTrue(
                Modifier.isVolatile(
                        JucLab11VolatileServiceState.class
                                .getDeclaredField("config")
                                .getModifiers()),
                "config 字段必须使用 volatile");

        ServiceConfig updated =
                new ServiceConfig(
                        2,
                        "https://api-v2.example",
                        800);
        state.updateConfig(updated);
        assertEquals(updated, state.currentConfig(), "配置快照更新错误");

        CountDownLatch workerStarted = new CountDownLatch(1);
        Thread worker = new Thread(() -> {
            workerStarted.countDown();
            while (state.isRunning()) {
                Thread.yield();
            }
        }, "volatile-stop-check");
        worker.start();
        workerStarted.await();
        state.requestStop();
        worker.join(1_000);
        if (worker.isAlive()) {
            worker.interrupt();
        }
        assertTrue(!worker.isAlive(), "工作线程没有观察到 volatile 停止信号");

        JucLab11VolatileServiceState counterState =
                new JucLab11VolatileServiceState(initial);
        runConcurrently(16, index -> {
            for (int count = 0; count < 1_000; count++) {
                counterState.recordProcessedRequest();
            }
        });
        assertEquals(
                16_000,
                counterState.getProcessedRequests(),
                "请求计数丢失；volatile 不能替代 AtomicInteger");
    }

    private static boolean purchaseImmediately(
            JucLab10SynchronizedInventory inventory) {
        try {
            return inventory.awaitAndPurchase(
                    1, 0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new AssertionError("并发购买被意外中断", interrupted);
        }
    }

    private static boolean purchaseImmediately(
            JucLab10LockInventory inventory) {
        try {
            return inventory.awaitAndPurchase(
                    1, 0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new AssertionError("并发购买被意外中断", interrupted);
        }
    }

    private static void awaitState(Thread thread,
                                   Thread.State expected,
                                   long timeout,
                                   TimeUnit unit) {
        long deadline =
                System.nanoTime()
                        + unit.toNanos(timeout);
        while (thread.getState() != expected
                && thread.isAlive()
                && System.nanoTime() < deadline) {
            Thread.yield();
        }
        assertEquals(
                expected,
                thread.getState(),
                "线程没有进入预期状态");
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
