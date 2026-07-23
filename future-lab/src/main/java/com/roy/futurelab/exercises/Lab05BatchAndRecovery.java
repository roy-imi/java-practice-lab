package com.roy.futurelab.exercises;

import com.roy.futurelab.common.DemoServices;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/*
 * 第 5 课：并行加载一组推荐商品，其中单个失败不拖垮整批结果。
 *
 * 【背景知识：Fan-out / Fan-in】
 *
 * 批量异步任务通常分为两个阶段：先把输入拆成多个独立任务并行执行
 * （fan-out），再等待所有任务完成并收集结果（fan-in）。
 * CompletableFuture.allOf(...) 用于 fan-in：所有子任务成功时它才成功，
 * 但它只返回完成信号，不会自动替你保存各项结果。
 *
 * 【API 详解】
 *
 * 1. itemFuture.whenComplete(observer)
 *
 * - observer：一个 BiConsumer，接收两个参数 result 和 error。
 * - 成功时：error 为 null，result 是任务结果。
 * - 失败时：error 保存异常；result 通常不可用。
 * - 返回值：一个继续保留原成功值或原异常的新 CompletableFuture。
 * - 用途：记录日志、指标等“观察行为”，不适合在这里静默吞掉异常。
 *
 * 2. itemFuture.exceptionally(recovery)
 *
 * - recovery：一个 Function，参数是异常，返回值是与正常结果相同类型的备用值。
 * - 返回值：新的 CompletableFuture。原任务成功时直接保留原值；
 *   原任务失败时使用 recovery 返回的降级值。
 * - 注意：recovery 自己再次抛异常时，新 Future 仍然失败。
 *
 * 3. CompletableFuture.allOf(futures)
 *
 * - futures：数量不固定的一组 CompletableFuture，Java 参数形式是可变参数，
 *   因此已有 List 时需要先转换成数组。
 * - 返回值：结果类型为 Void 的 CompletableFuture，只表示“全部完成”。
 * - 成功条件：所有输入 Future 都成功；任意输入失败时，allOf 也会失败。
 * - 空数组：直接得到一个已经成功完成的 Future。
 *
 * 4. future.join()
 *
 * - 参数：没有参数。
 * - 返回值：该子任务的结果。
 * - 本课为何可以在 allOf 之后使用：allOf 已经确认全部完成，
 *   所以后续按原列表顺序 join() 只是在读取结果，不会再次等待。
 *
 * 5. future.handle(handler)
 *
 * - handler：一个 BiFunction，同时接收正常结果和异常，并返回一个新结果。
 * - 与 exceptionally 的区别：handle 无论成功失败都会运行；exceptionally 只在失败时运行。
 * - 本课不要求使用 handle，但可以用它实现“成功和失败统一映射”的扩展练习。
 *
 * 【为什么 allOf 只返回 Void】
 *
 * 传入 allOf 的 Future 可以拥有不同的结果类型，因此 allOf 只负责发出
 * “全部完成”的信号。若想得到列表，需要保留原来的 Future 集合；
 * allOf 完成后再从这些 Future 中取值。此时它们已经完成，join() 不会再等待。
 *
 * 【观察异常与恢复异常】
 *
 * - whenComplete：成功或失败都会执行，适合日志和指标；
 *   它通常不改变原来的结果或异常。
 * - exceptionally：只在失败时执行，把异常转换成同类型的备用值。
 * - handle：成功或失败都会执行，并且可以产生新结果；适合需要统一转换的场景。
 *
 * 如果某个子任务的异常没有先恢复，allOf 也会异常完成，整批结果就无法正常进入
 * 后续收集阶段。本课采用“每项独立降级”的策略，使一个商品失败不会拖垮其他商品。
 *
 * 【完成顺序不等于输入顺序】
 *
 * 并发任务的完成顺序由耗时决定。如果业务要求输出保持输入顺序，
 * 应按原 Future 列表的顺序收集，而不是按照回调触发顺序写入普通列表。
 */
public final class Lab05BatchAndRecovery {
    private Lab05BatchAndRecovery() {
    }

    public static CompletableFuture<List<String>> loadRecommendations(
            DemoServices services,
            Executor executor,
            List<String> productNames,
            Consumer<String> eventLog) {
        /*
         * TODO 你的任务：
         * 1. 为每个商品创建 supplyAsync 任务；
         * 2. 每个任务用 whenComplete 记录成功/失败（不要改变结果）；
         * 3. 每个任务用 exceptionally 把失败降级为“商品名：暂不可用”；
         * 4. 用 allOf 等全部任务结束；
         * 5. allOf 只给 Void，因此在后续 thenApply 中从原 Future 列表 join 结果；
         * 6. 返回的 List 要保持输入顺序。
         */
        List<CompletableFuture<String>> list = new ArrayList<>();
        for (String productName : productNames) {
            CompletableFuture<String> itemCompletableFuture = CompletableFuture
                    .supplyAsync(() -> services.fetchRecommendation(productName))
                    .whenComplete((result, error) -> eventLog.accept(
                            productName + (error == null ? "加载成功": "加载失败")))
                    .exceptionally(error -> productName + "暂不可用");
            list.add(itemCompletableFuture);
        }

        // CompletableFuture.allOf(futures)
        // futures：数量不固定的一组 CompletableFuture，Java 参数形式是可变参数，
        // 因此已有 List 时需要先转换成数组。
        CompletableFuture<?>[] completableFutures =
                list.toArray(new CompletableFuture<?>[list.size()]);

        return CompletableFuture.allOf(completableFutures)
                .thenApply(ignored -> list.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }
}
