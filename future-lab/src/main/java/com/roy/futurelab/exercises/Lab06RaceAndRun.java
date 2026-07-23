package com.roy.futurelab.exercises;

import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.model.ShopQuote;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/*
 * 第 6 课：使用最先返回的商店报价，并异步写一条审计日志。
 *
 * 【背景知识：等待全部与竞速选择】
 *
 * allOf 适合必须拿到全部结果的场景；anyOf 则在任意一个任务
 * 完成时立刻完成，适合镜像服务竞速、快速兜底等“第一个可用结果优先”的策略。
 * 由于输入任务可能具有不同结果类型，anyOf 返回的结果类型是 Object，
 * 类型安全需要由调用方恢复。
 *
 * 【API 详解】
 *
 * 1. CompletableFuture.anyOf(futures)
 *
 * - futures：数量不固定的一组 CompletableFuture，通常由各个竞速任务组成。
 * - 返回值：结果类型为 Object 的 CompletableFuture。
 * - 完成条件：最先结束的任务决定 anyOf 的结果。
 * - 重要细节：最先结束的任务如果失败，anyOf 会立即失败；
 *   它等待的是“第一个完成”，不是“第一个成功”。
 * - 空数组：返回一个永远不会自动完成的 Future，因此调用前应保证至少有一个任务。
 *
 * 2. anyFuture.thenApply(converter)
 *
 * - converter：接收 anyOf 返回的 Object，把它检查或转换成业务类型。
 * - 返回值：具有明确业务类型的新 CompletableFuture。
 * - 注意：强制类型转换只有在所有输入 Future 的结果类型一致时才安全。
 *
 * 3. CompletableFuture.runAsync(action, executor)
 *
 * - action：一个 Runnable，也就是“无参数、无返回值”的任务。
 *   Lambda 形式通常是 () -> 执行一次写日志或发送通知。
 * - executor：执行 action 的线程池。
 * - 返回值：结果类型为 Void 的 CompletableFuture，可表示 action 是否完成或失败。
 *
 * 4. auditWriter.accept(message)
 *
 * - message：交给 Consumer 的审计日志字符串。
 * - 返回值：没有返回值。
 * - Consumer 由外部传入，因此本方法只决定“写什么”，不绑定控制台、文件或数据库。
 *
 * 【anyOf 的两个重要边界】
 *
 * - “最先完成”只代表延迟最低，不代表价格最低、数据最新或业务质量最好。
 * - anyOf 完成后，其他落后任务不会自动取消，通常仍会继续占用线程和服务资源。
 *   真实系统若要取消它们，需要保存原 Future 并设计明确的取消策略。
 *
 * 【supplyAsync 与 runAsync】
 *
 * supplyAsync 用于需要产生结果的任务，对应 Supplier；
 * runAsync 用于只执行副作用、没有返回值的任务，对应 Runnable，
 * 所以返回的 Future 结果类型是 Void。写审计日志属于后一种场景。
 *
 * 【显式线程池】
 *
 * 不传 Executor 时，异步工厂方法默认使用公共 ForkJoinPool。
 * 业务中的阻塞 I/O 可能长期占用公共线程，因此本课显式使用调用方传入的线程池，
 * 也让线程池的容量、命名和生命周期更容易控制。
 *
 * 注意：“最先返回”不等于“价格最低”。这是延迟与业务质量之间的真实取舍。
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
