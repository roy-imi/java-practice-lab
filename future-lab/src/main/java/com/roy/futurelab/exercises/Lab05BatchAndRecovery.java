package com.roy.futurelab.exercises;

import com.roy.futurelab.common.DemoServices;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/*
 * 第 5 课：并行加载推荐商品，并让单项失败不拖垮整批结果。
 *
 * 课前文档：docs/guides/FUTURE_API_GUIDE.md（第 5 课）
 * 练习重点：whenComplete、exceptionally、allOf 和按输入顺序收集结果。
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
