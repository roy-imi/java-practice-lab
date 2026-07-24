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
import com.roy.juclab.model.RequestContext;
import com.roy.juclab.model.ServiceConfig;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class JucLearningConsole {
    private JucLearningConsole() {
    }

    public static void main(String[] args) throws InterruptedException {
        int lesson = args.length == 0 ? 1 : Integer.parseInt(args[0]);
        System.out.println("=== 运行 JUC 第 " + lesson + " 课 ===");

        try {
            runLesson(lesson);
        } catch (UnsupportedOperationException todo) {
            System.out.println(todo.getMessage());
            System.out.println(
                    "需要时查看 "
                            + String.format(
                                    "docs/hints/lab-%02d.md",
                                    lesson));
        }
    }

    private static void runLesson(int lesson) throws InterruptedException {
        switch (lesson) {
            case 1:
                Thread worker = JucLab01ThreadLifecycle.startTask(
                        "order-worker-1",
                        () -> System.out.println(
                                "任务线程：" + Thread.currentThread().getName()));
                JucLab01ThreadLifecycle.waitFor(worker);
                System.out.println("工作线程已结束：" + !worker.isAlive());
                return;
            case 2:
                JucLab02AtomicInventory inventory =
                        new JucLab02AtomicInventory(5);
                for (int index = 0; index < 6; index++) {
                    System.out.println(
                            "购买结果：" + inventory.tryPurchase(1));
                }
                System.out.println(
                        "剩余库存：" + inventory.getRemainingStock());
                return;
            case 3:
                JucLab03PriceCache cache = new JucLab03PriceCache();
                cache.put("SKU-1", 19_900);
                System.out.println("价格：" + cache.get("SKU-1"));
                System.out.println("快照：" + cache.snapshot());
                return;
            case 4:
                JucLab04RequestStatistics statistics =
                        new JucLab04RequestStatistics();
                statistics.record("/orders");
                statistics.record("/orders");
                System.out.println("请求统计：" + statistics.snapshot());
                return;
            case 5:
                Runnable controlled = JucLab05ConcurrencyGate.waitForStart(
                        new CountDownLatch(0),
                        JucLab05ConcurrencyGate.limitConcurrency(
                                new Semaphore(1),
                                () -> System.out.println("已获得许可并执行")));
                controlled.run();
                return;
            case 6:
                System.out.println("处理结果："
                        + JucLab06BlockingQueuePipeline.process(
                                Arrays.asList("order-a", "order-b", "order-c"),
                                2,
                                String::toUpperCase));
                return;
            case 7:
                ThreadPoolExecutor executor =
                        JucLab07ThreadPoolFactory.create(
                                1, 2, 2, "checkout");
                try {
                    CountDownLatch completed = new CountDownLatch(1);
                    executor.execute(() -> {
                        System.out.println(
                                "线程池任务："
                                        + Thread.currentThread().getName());
                        completed.countDown();
                    });
                    completed.await();
                    System.out.println("优雅关闭："
                            + JucLab07ThreadPoolFactory.shutdownGracefully(
                                    executor, 1, TimeUnit.SECONDS));
                } finally {
                    if (!executor.isShutdown()) {
                        executor.shutdownNow();
                    }
                }
                return;
            case 8:
                JucLab08FlashSaleService service =
                        new JucLab08FlashSaleService(2);
                System.out.println(service.purchase("request-1"));
                System.out.println(service.purchase("request-1"));
                System.out.println(service.purchase("request-2"));
                System.out.println(service.purchase("request-3"));
                System.out.println(
                        "剩余库存：" + service.getRemainingStock());
                return;
            case 9:
                RequestContext requestContext =
                        new RequestContext("request-1001", "user-42");
                JucLab09ThreadLocalContext.set(requestContext);
                Runnable contextAware =
                        JucLab09ThreadLocalContext.wrap(
                                () -> System.out.println(
                                        "任务中的上下文："
                                                + JucLab09ThreadLocalContext
                                                .current()
                                                .orElse(null)));
                JucLab09ThreadLocalContext.clear();

                contextAware.run();
                System.out.println(
                        "任务结束后的上下文："
                                + JucLab09ThreadLocalContext.current());
                return;
            case 10:
                JucLab10SynchronizedInventory synchronizedInventory =
                        new JucLab10SynchronizedInventory(2);
                System.out.println(
                        "synchronized 购买："
                                + synchronizedInventory.awaitAndPurchase(
                                        1, 0, TimeUnit.MILLISECONDS));
                System.out.println(
                        "synchronized 剩余："
                                + synchronizedInventory
                                .getRemainingStock());

                JucLab10LockInventory lockInventory =
                        new JucLab10LockInventory(2, false);
                System.out.println(
                        "ReentrantLock 购买："
                                + lockInventory.awaitAndPurchase(
                                        1, 0, TimeUnit.MILLISECONDS));
                System.out.println(
                        "ReentrantLock 剩余："
                                + lockInventory.getRemainingStock());
                return;
            case 11:
                ServiceConfig initialConfig =
                        new ServiceConfig(
                                1,
                                "https://api-v1.example",
                                1_000);
                JucLab11VolatileServiceState serviceState =
                        new JucLab11VolatileServiceState(initialConfig);
                System.out.println(
                        "初始配置：" + serviceState.currentConfig());
                serviceState.updateConfig(
                        new ServiceConfig(
                                2,
                                "https://api-v2.example",
                                800));
                System.out.println(
                        "更新配置：" + serviceState.currentConfig());
                serviceState.recordProcessedRequest();
                System.out.println(
                        "已处理请求："
                                + serviceState.getProcessedRequests());
                serviceState.requestStop();
                System.out.println(
                        "仍在运行：" + serviceState.isRunning());
                return;
            default:
                throw new IllegalArgumentException("课次只能是 1 到 11");
        }
    }
}
