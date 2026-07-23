package com.roy.futurelab.exercises;

import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.model.ShopQuote;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/*
 * 第 6 课：使用最先返回的商店报价，并异步写审计日志。
 *
 * 课前文档：docs/guides/FUTURE_API_GUIDE.md（第 6 课）
 * 练习重点：anyOf、runAsync，以及“最先完成”与“最优结果”的区别。
 */
public final class Lab06RaceAndRun {
    private Lab06RaceAndRun() {
    }

    public static CompletableFuture<ShopQuote> firstCompletedQuote(DemoServices services,
                                                                  Executor executor,
                                                                  List<String> shopNames) {
        /*
         * TODO 你的任务：
         * - 每家店用 supplyAsync 查询；
         * - 把所有任务交给 anyOf；
         * - anyOf 的结果类型是 Object，用 thenApply 转成 ShopQuote；
         * - 返回流水线，不要阻塞。
         */
        if (shopNames.isEmpty()) {
            throw new IllegalArgumentException("至少要有一家商店");
        }

        CompletableFuture<?>[] quoteFutures = shopNames.stream()
                .map(shopName -> CompletableFuture.supplyAsync(
                        () -> services.queryShopQuote(shopName), executor))
                .toArray(CompletableFuture<?>[]::new);

        return CompletableFuture.anyOf(quoteFutures)
                .thenApply(value -> (ShopQuote) value);
    }

    public static CompletableFuture<Void> writeAuditLogAsync(ShopQuote quote,
                                                             Executor executor,
                                                             Consumer<String> auditWriter) {
        /*
         * TODO 你的任务：
         * - 用 runAsync 执行没有返回值的 auditWriter；
         * - 日志内容：“采用报价：” + quote；
         * - 使用传入的 executor。
         */
        return CompletableFuture.runAsync(
                    () -> auditWriter.accept("采用报价：" + quote), executor
                );
    }
}
