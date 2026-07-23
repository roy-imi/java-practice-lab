package com.roy.futurelab.exercises;

import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.common.Money;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/*
 * 第 3 课：把“查询价格 -> 打九折 -> 格式化 -> 通知”写成异步流水线。
 *
 * 【背景知识：Future 为什么不够方便】
 *
 * 普通 Future 能表示“以后会有一个结果”，但不擅长描述“结果出来后继续做什么”。
 * 调用方往往只能主动 get()，取到结果后再执行下一步。
 * CompletableFuture 把异步处理拆成多个 stage（阶段），每个阶段声明：
 * 上一步完成后，如何转换或消费它的结果。
 *
 * 【创建、转换和消费】
 *
 * - supplyAsync：异步执行一个有返回值的 Supplier，产生流水线的第一个值。
 * - thenApply：把一种类型转换为另一种类型，类似集合的 map；
 *   它会把返回值继续交给下一阶段。
 * - thenAccept：消费结果但不产生新值，所以返回的 Future 结果类型是 Void。
 *
 * 【API 详解】
 *
 * 1. CompletableFuture.supplyAsync(supplier, executor)
 *
 * - supplier：一个 Supplier，也就是“无参数、有返回值”的任务。
 *   Lambda 形式通常是 () -> 计算并返回一个值。
 * - executor：负责执行 supplier 的线程池。
 * - 返回值：代表 supplier 最终结果的 CompletableFuture。
 * - 注意：supplier 不能直接抛受检异常；如确实需要，要在 Lambda 内处理或转换异常。
 *
 * 2. previousFuture.thenApply(transformer)
 *
 * - previousFuture：提供上一步结果的 Future。
 * - transformer：一个 Function，接收上一步结果，返回转换后的新结果。
 *   Lambda 形式通常是 previousValue -> newValue。
 * - 返回值：保存新结果的新 CompletableFuture，原 Future 不会被修改。
 * - 执行条件：上一步成功后才执行；上一步失败时跳过转换并继续传播异常。
 *
 * 3. previousFuture.thenAccept(action)
 *
 * - action：一个 Consumer，接收上一步结果，只执行操作，不返回新业务值。
 *   Lambda 形式通常是 value -> 使用这个值完成通知或输出。
 * - 返回值：结果类型为 Void 的 CompletableFuture，可用于等待“消费动作已经完成”。
 * - 执行条件：上一步成功后才执行；action 自己抛异常时，返回的 Future 会失败。
 *
 * 4. future.join()
 *
 * - 参数：没有参数。
 * - 返回值：Future 的最终结果；尚未完成时会阻塞当前线程。
 * - 异常：失败时抛非受检的 CompletionException，真实原因在 getCause() 中。
 * - 与 get() 的区别：get() 抛受检异常，join() 更适合流水线最外层取得最终结果。
 *
 * 【非阻塞组合】
 *
 * 构建流水线并不等于等待流水线。练习方法应直接返回 CompletableFuture，
 * 让最外层调用者决定何时使用 join() 或 get()。
 * 如果在每一个阶段后立即等待，就会重新把异步流程写成同步流程。
 *
 * 【线程与异常传播】
 *
 * 不带 Async 后缀的 thenApply / thenAccept，通常由完成上一阶段的线程继续执行；
 * 带 Async 后缀的版本会重新调度任务。
 * 任一阶段抛出异常时，后续正常转换会被跳过，异常状态沿流水线向后传播，
 * 直到有异常处理阶段接住它。
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
