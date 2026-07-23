package com.roy.futurelab.exercises;

import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.common.Money;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/*
 * 第 3 课：把“查询价格 -> 打九折 -> 格式化 -> 通知”写成异步流水线。
 *
 * 课前文档：docs/guides/FUTURE_API_GUIDE.md（第 3 课）
 * 练习重点：supplyAsync、thenApply、thenAccept 和非阻塞组合。
 */
public final class Lab03Pipeline {
    private Lab03Pipeline() {
    }

    public static CompletableFuture<String> buildVipPriceLabel(DemoServices services,
                                                               Executor executor,
                                                               String productName) {
        /*
         * TODO 你的任务：
         * - supplyAsync 异步查询分为单位的价格；
         * - 第一个 thenApply 计算九折价；
         * - 第二个 thenApply 格式化为“VIP价：¥179.10”；
         * - 返回整条流水线，不要在这里 get()/join()。
         */
        return CompletableFuture
                .supplyAsync(() -> services.queryPriceCents(productName), executor)
                .thenApply(priceCents -> priceCents * 90 / 100)
                .thenApply(vipPrice -> "VIP价：¥"  + Money.format(vipPrice));
    }

    public static CompletableFuture<Void> notifyWhenReady(CompletableFuture<String> labelFuture,
                                                          Consumer<String> notifier) {
        /*
         * TODO 你的任务：
         * - 用 thenAccept 把价格标签交给 notifier；
         * - 观察返回值为什么是 CompletableFuture<Void>。
         */
        return labelFuture.thenAccept(notifier);
    }
}
