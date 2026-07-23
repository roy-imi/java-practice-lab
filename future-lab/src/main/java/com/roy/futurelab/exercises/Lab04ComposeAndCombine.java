package com.roy.futurelab.exercises;

import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.model.CheckoutSummary;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/*
 * 第 4 课：构建结算页。
 *
 * 课前文档：docs/guides/FUTURE_API_GUIDE.md（第 4 课）
 * 练习重点：依赖任务使用 thenCompose，独立任务使用 thenCombine。
 *
 * 依赖关系：
 * - 用户等级 -> 会员折扣；
 * - 商品价格、折扣链和运费可以独立启动。
 */
public final class Lab04ComposeAndCombine {
    private Lab04ComposeAndCombine() {
    }

    public static CompletableFuture<CheckoutSummary> buildCheckout(DemoServices services,
                                                                   Executor executor,
                                                                   String productName,
                                                                   String userId,
                                                                   String city) {
        /*
         * TODO 你的任务：
         * 1. 创建 priceFuture 和 deliveryFuture；
         * 2. 创建 userLevelFuture；
         * 3. 用 thenCompose：得到等级后，再异步 queryDiscountPercent；
         * 4. 用 thenCombine 合并 price + discount；
         * 5. 再与 deliveryFuture 合并，创建 CheckoutSummary；
         * 6. 全程不要在流水线中 get()/join()。
         */
        // 商品价格
        CompletableFuture<Integer> priceFuture = CompletableFuture
                .supplyAsync(() -> services.queryPriceCents(productName), executor);

        // 运费
        CompletableFuture<Integer> deliveryFuture = CompletableFuture
                .supplyAsync(() -> services.queryDeliveryFeeCents(city), executor);

        // 用户等级
        CompletableFuture<String> userLevelFuture = CompletableFuture
                .supplyAsync(() -> services.queryUserLevel(userId), executor);

        // 会员折扣
        CompletableFuture<Integer> discountFuture = userLevelFuture
                .thenCompose(level -> CompletableFuture
                        .supplyAsync(() -> services.queryDiscountPercent(level)));

        CompletableFuture<int[]> priceAndDiscount = priceFuture
                .thenCombine(discountFuture,
                        (priceCent, discountPercent) -> new int[]{priceCent, discountPercent});
        return priceAndDiscount
                .thenCombine(deliveryFuture,
                        (values, deliveryFeeCents) -> new CheckoutSummary(values[0], values[1], deliveryFeeCents));
    }
}
