package com.roy.futurelab;

import com.roy.futurelab.common.DemoExecutors;
import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.exercises.Lab01FutureBasics;
import com.roy.futurelab.exercises.Lab02TimeoutAndCancel;
import com.roy.futurelab.exercises.Lab03Pipeline;
import com.roy.futurelab.exercises.Lab04ComposeAndCombine;
import com.roy.futurelab.exercises.Lab05BatchAndRecovery;
import com.roy.futurelab.exercises.Lab06RaceAndRun;
import com.roy.futurelab.model.CheckoutSummary;
import com.roy.futurelab.model.ProductSummary;
import com.roy.futurelab.model.ShopQuote;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 只调用 exercises 包，不读取 answers 包。
 * 完成某一课后运行本类，验证行为而不是代码写法。
 */
public final class ExerciseCheck {
    private ExerciseCheck() {
    }

    public static void main(String[] args) {
        int lesson = parseLesson(args);
        DemoServices services = new DemoServices();
        ExecutorService executor =
                DemoExecutors.newPool("exercise-check-" + lesson, 8);
        int exitCode = 0;

        try {
            check(lesson, services, executor);
            System.out.println("[通过] 第 " + lesson + " 课练习符合预期。");
        } catch (UnsupportedOperationException todo) {
            exitCode = 1;
            System.out.println("[未完成] " + todo.getMessage());
        } catch (AssertionError wrongResult) {
            exitCode = 1;
            System.out.println("[未通过] " + wrongResult.getMessage());
        } catch (Exception failure) {
            exitCode = 1;
            System.out.println("[运行异常] " + failure);
        } finally {
            DemoExecutors.shutdown(executor);
        }

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static void check(int lesson,
                              DemoServices services,
                              ExecutorService executor) throws Exception {
        switch (lesson) {
            case 1:
                checkLab01(services, executor);
                return;
            case 2:
                checkLab02(services, executor);
                return;
            case 3:
                checkLab03(services, executor);
                return;
            case 4:
                checkLab04(services, executor);
                return;
            case 5:
                checkLab05(services, executor);
                return;
            case 6:
                checkLab06(services, executor);
                return;
            default:
                throw new IllegalArgumentException("课次只能是 1 到 6");
        }
    }

    private static void checkLab01(DemoServices services,
                                   ExecutorService executor) throws Exception {
        long startedAt = System.currentTimeMillis();
        ProductSummary result = Lab01FutureBasics.loadProductSummary(
                services, executor, "机械键盘", ignored -> {
                });
        long elapsed = System.currentTimeMillis() - startedAt;

        assertEquals(19_900, result.getPriceCents(), "价格不正确");
        assertEquals(12, result.getStock(), "库存不正确");
        assertTrue(elapsed < 800,
                "耗时 " + elapsed + "ms，两个任务可能没有并行提交");
    }

    private static void checkLab02(DemoServices services,
                                   ExecutorService executor) throws Exception {
        long startedAt = System.currentTimeMillis();
        String timeoutResult = Lab02TimeoutAndCancel.loadReviewWithTimeout(
                services, executor, "机械键盘", 120);
        long elapsed = System.currentTimeMillis() - startedAt;

        assertEquals("评价加载超时", timeoutResult, "超时降级文案不正确");
        assertTrue(elapsed < 600, "超时等待明显过长：" + elapsed + "ms");

        String failureResult = Lab02TimeoutAndCancel.loadReviewWithTimeout(
                services, executor, "评价故障商品", 2_000);
        assertEquals(
                "评价服务异常：评价服务返回 503",
                failureResult,
                "没有正确拆开 ExecutionException");
    }

    private static void checkLab03(DemoServices services,
                                   ExecutorService executor) {
        String label = Lab03Pipeline.buildVipPriceLabel(
                services, executor, "机械键盘").join();
        assertEquals("VIP价：¥179.10", label, "价格流水线结果不正确");

        AtomicReference<String> notified = new AtomicReference<>();
        Lab03Pipeline.notifyWhenReady(
                Lab03Pipeline.buildVipPriceLabel(
                        services, executor, "机械键盘"),
                notified::set
        ).join();
        assertEquals("VIP价：¥179.10", notified.get(), "thenAccept 未正确通知");
    }

    private static void checkLab04(DemoServices services,
                                   ExecutorService executor) {
        CheckoutSummary result = Lab04ComposeAndCombine.buildCheckout(
                services, executor, "机械键盘", "user-001", "上海").join();
        assertEquals(15, result.getDiscountPercent(), "会员折扣不正确");
        assertEquals(18_115, result.getTotalCents(), "结算总价不正确");
    }

    private static void checkLab05(DemoServices services,
                                   ExecutorService executor) {
        List<String> events = new CopyOnWriteArrayList<>();
        List<String> results = Lab05BatchAndRecovery.loadRecommendations(
                services,
                executor,
                Arrays.asList("无线鼠标", "已下架耳机", "显示器"),
                events::add
        ).join();

        assertEquals(3, results.size(), "结果数量不正确");
        assertTrue(results.get(0).startsWith("无线鼠标"), "输入顺序被改变");
        assertEquals("已下架耳机：暂不可用", results.get(1), "失败项没有降级");
        assertTrue(results.get(2).startsWith("显示器"), "输入顺序被改变");
        assertEquals(3, events.size(), "whenComplete 事件数量不正确");
    }

    private static void checkLab06(DemoServices services,
                                   ExecutorService executor) {
        ShopQuote quote = Lab06RaceAndRun.firstCompletedQuote(
                services, executor, Arrays.asList("A店", "B店", "C店")).join();
        assertEquals("B店", quote.getShopName(), "没有取到最先完成的 B 店");

        AtomicReference<String> audit = new AtomicReference<>();
        Lab06RaceAndRun.writeAuditLogAsync(
                quote, executor, audit::set).join();
        assertTrue(audit.get() != null && audit.get().startsWith("采用报价：B店"),
                "runAsync 没有写入预期日志");
    }

    private static int parseLesson(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                    "用法：ExerciseCheck <1-6>，例如 ExerciseCheck 1");
        }
        return Integer.parseInt(args[0]);
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
