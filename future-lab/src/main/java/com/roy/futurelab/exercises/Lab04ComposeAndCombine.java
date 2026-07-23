package com.roy.futurelab.exercises;

import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.model.CheckoutSummary;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/*
 * 第 4 课：构建结算页。
 *
 * 【背景知识：先画数据依赖，再选择组合操作】
 *
 * CompletableFuture 的价值不只是“开线程”，更重要的是把任务之间的依赖关系写成数据流。
 * 开始编码前，应先判断两个任务是前后依赖，还是彼此独立：
 *
 * - 后一步必须使用前一步的结果：串联，选择 thenCompose。
 * - 两个任务可各自执行，最后才需要两个结果：汇合，选择 thenCombine。
 *
 * 【API 详解】
 *
 * 1. previousFuture.thenCompose(nextAsyncTask)
 *
 * - previousFuture：保存前一步结果的 Future。
 * - nextAsyncTask：一个 Function，参数是前一步结果，返回值必须是另一个异步阶段。
 *   Lambda 形式通常是 previousValue -> 启动并返回下一个 CompletableFuture。
 * - 返回值：压平后的新 CompletableFuture，而不是两层 Future。
 * - 执行时机：前一步成功后才调用 nextAsyncTask。
 * - 使用场景：第二个异步任务必须先知道第一个任务的结果。
 *
 * 2. firstFuture.thenCombine(secondFuture, combiner)
 *
 * - secondFuture：另一个可以独立运行的异步任务。
 * - combiner：一个 BiFunction，第一个参数来自 firstFuture，
 *   第二个参数来自 secondFuture，返回合并后的新值。
 *   Lambda 形式通常是 (firstValue, secondValue) -> combinedValue。
 * - 返回值：保存 combinedValue 的新 CompletableFuture。
 * - 执行时机：两个 Future 都成功完成后执行；不要求哪一个先完成。
 *
 * 3. thenComposeAsync / thenCombineAsync
 *
 * - 带 Async 的版本会把后续函数重新交给线程池调度。
 * - 可以额外传入 Executor 来指定线程池；不传时通常使用公共 ForkJoinPool。
 * - 本课重点是任务依赖关系，不要因为看到“异步”就无条件选择 Async 版本。
 *
 * 【thenCompose：压平嵌套 Future】
 *
 * 查询用户等级后，才能按等级异步查询折扣。第二个查询本身也返回 CompletableFuture。
 * 如果使用 thenApply，结果会变成“两层 CompletableFuture”。
 * thenCompose 会把这两层结构压平成一层，它相当于异步世界的 flatMap。
 *
 * 【thenCombine：汇合独立结果】
 *
 * 价格、运费以及折扣链可以尽早同时启动。thenCombine 不规定谁先完成，
 * 它会等两边都成功后，把两个结果交给合并函数。若其中一边失败，
 * 合并后的阶段也会以异常状态完成。
 *
 * 【常见误区】
 *
 * 不要为了拿到中间结果而在流水线中调用 get() 或 join()。
 * 阻塞会隐藏原本的数据依赖，并可能造成线程饥饿。正确的方向是继续组合 Future，
 * 直到系统最外层的同步边界再统一等待。
 *
 * 依赖关系：
 * - 用户等级 -> 会员折扣：后者依赖前者，用 thenCompose；
 * - 商品价格、会员折扣、运费：彼此独立查询，最后用 thenCombine 汇合。
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
