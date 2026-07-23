package com.roy.futurelab.exercises;

import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.model.ProductSummary;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/*
 * 第 1 课：用 Future 同时查询价格与库存。
 *
 * 课前文档：docs/guides/FUTURE_API_GUIDE.md（第 1 课）
 * 练习重点：submit、Future.get、isDone，以及先提交再等待。
 *
 * 约束：
 * 1. 必须先提交两个任务，再等待任意一个结果；
 * 2. 用 trace 输出两个 Future 刚提交后的 isDone 状态；
 * 3. 不要在这里关闭 executor，它由调用方创建并关闭。
 */
public final class Lab01FutureBasics {
    private Lab01FutureBasics() {
    }

    public static ProductSummary loadProductSummary(DemoServices services,
                                                    ExecutorService executor,
                                                    String productName,
                                                    Consumer<String> trace)
            throws Exception {
        /*
         * TODO 你的任务：
         * - 把 queryPriceCents 和 queryStock 分别提交给 executor；
         * - 观察 Future<Integer>；
         * - 两个任务都提交后，再通过 Future 取得结果；
         * - 组装并返回 ProductSummary。
         *
         * 先运行本课，再按 docs/hints/lab-01.md 逐级查看提示。
         */

        Future<Integer> priceFuture = executor.submit(() -> services.queryPriceCents(productName));
        Future<Integer> stockFuture = executor.submit(() -> services.queryStock(productName));

        int priceCent = priceFuture.get();
        int stock = stockFuture.get();

        return new ProductSummary(productName, priceCent, stock);
    }
}
