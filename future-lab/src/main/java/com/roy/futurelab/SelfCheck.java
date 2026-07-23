package com.roy.futurelab;

import com.roy.futurelab.answers.Lab01FutureBasicsAnswer;
import com.roy.futurelab.answers.Lab02TimeoutAndCancelAnswer;
import com.roy.futurelab.answers.Lab03PipelineAnswer;
import com.roy.futurelab.answers.Lab04ComposeAndCombineAnswer;
import com.roy.futurelab.answers.Lab05BatchAndRecoveryAnswer;
import com.roy.futurelab.answers.Lab06RaceAndRunAnswer;
import com.roy.futurelab.common.DemoExecutors;
import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.model.CheckoutSummary;
import com.roy.futurelab.model.ProductSummary;
import com.roy.futurelab.model.ShopQuote;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

public final class SelfCheck {
    private SelfCheck() {
    }

    public static void main(String[] args) throws Exception {
        DemoServices services = new DemoServices();
        ExecutorService executor = DemoExecutors.newPool("self-check", 8);

        try {
            checkLab01(services, executor);
            checkLab02(services, executor);
            checkLab03(services, executor);
            checkLab04(services, executor);
            checkLab05(services, executor);
            checkLab06(services, executor);
            System.out.println();
            System.out.println("全部通过：6/6。项目与参考答案可以正常运行。");
        } finally {
            DemoExecutors.shutdown(executor);
        }
    }

    private static void checkLab01(DemoServices services,
                                   ExecutorService executor) throws Exception {
        long startedAt = System.currentTimeMillis();
        ProductSummary result = Lab01FutureBasicsAnswer.loadProductSummary(
                services, executor, "机械键盘", ignored -> {
                });
        long elapsed = System.currentTimeMillis() - startedAt;

        assertEquals(19_900, result.getPriceCents(), "第 1 课价格");
        assertEquals(12, result.getStock(), "第 1 课库存");
        assertTrue(elapsed < 800, "第 1 课应并行执行，实际 " + elapsed + "ms");
        pass(1, "Future 并行查询");
    }

    private static void checkLab02(DemoServices services,
                                   ExecutorService executor) throws Exception {
        String timeout = Lab02TimeoutAndCancelAnswer.loadReviewWithTimeout(
                services, executor, "机械键盘", 120);
        assertEquals("评价加载超时", timeout, "第 2 课超时降级");

        String failed = Lab02TimeoutAndCancelAnswer.loadReviewWithTimeout(
                services, executor, "评价故障商品", 2_000);
        assertEquals("评价服务异常：评价服务返回 503", failed, "第 2 课异常拆包");
        pass(2, "超时、取消与异常");
    }

    private static void checkLab03(DemoServices services,
                                   ExecutorService executor) {
        String label = Lab03PipelineAnswer.buildVipPriceLabel(
                services, executor, "机械键盘").join();
        assertEquals("VIP价：¥179.10", label, "第 3 课价格标签");

        AtomicReference<String> notified = new AtomicReference<>();
        Lab03PipelineAnswer.notifyWhenReady(
                Lab03PipelineAnswer.buildVipPriceLabel(
                        services, executor, "机械键盘"),
                notified::set
        ).join();
        assertEquals("VIP价：¥179.10", notified.get(), "第 3 课通知");
        pass(3, "CompletableFuture 流水线");
    }

    private static void checkLab04(DemoServices services,
                                   ExecutorService executor) {
        CheckoutSummary result = Lab04ComposeAndCombineAnswer.buildCheckout(
                services, executor, "机械键盘", "user-001", "上海").join();
        assertEquals(15, result.getDiscountPercent(), "第 4 课折扣");
        assertEquals(18_115, result.getTotalCents(), "第 4 课总价");
        pass(4, "依赖串联与独立合并");
    }

    private static void checkLab05(DemoServices services,
                                   ExecutorService executor) {
        List<String> events = new CopyOnWriteArrayList<>();
        List<String> results = Lab05BatchAndRecoveryAnswer.loadRecommendations(
                services,
                executor,
                Arrays.asList("无线鼠标", "已下架耳机", "显示器"),
                events::add
        ).join();

        assertEquals(3, results.size(), "第 5 课结果数量");
        assertEquals("已下架耳机：暂不可用", results.get(1), "第 5 课单项降级");
        assertEquals(3, events.size(), "第 5 课事件数量");
        pass(5, "批量聚合与失败恢复");
    }

    private static void checkLab06(DemoServices services,
                                   ExecutorService executor) {
        ShopQuote quote = Lab06RaceAndRunAnswer.firstCompletedQuote(
                services, executor, Arrays.asList("A店", "B店", "C店")).join();
        assertEquals("B店", quote.getShopName(), "第 6 课最快商店");

        AtomicReference<String> audit = new AtomicReference<>();
        Lab06RaceAndRunAnswer.writeAuditLogAsync(
                quote, executor, audit::set).join();
        assertTrue(audit.get().startsWith("采用报价：B店"), "第 6 课审计日志");
        pass(6, "竞速查询与无返回值任务");
    }

    private static void pass(int lesson, String topic) {
        System.out.println("[通过] 第 " + lesson + " 课：" + topic);
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
                    message + "，期望 " + expected + "，实际 " + actual);
        }
    }
}
