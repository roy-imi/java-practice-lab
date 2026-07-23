package com.roy.futurelab.answers;

import com.roy.futurelab.common.DemoServices;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class Lab05BatchAndRecoveryAnswer {
    private Lab05BatchAndRecoveryAnswer() {
    }

    public static CompletableFuture<List<String>> loadRecommendations(
            DemoServices services,
            Executor executor,
            List<String> productNames,
            Consumer<String> eventLog) {
        List<CompletableFuture<String>> itemFutures = new ArrayList<>();

        for (String productName : productNames) {
            CompletableFuture<String> itemFuture = CompletableFuture
                    .supplyAsync(
                            () -> services.fetchRecommendation(productName),
                            executor)
                    .whenComplete((result, error) -> eventLog.accept(
                            productName + (error == null ? " 加载成功" : " 加载失败")))
                    .exceptionally(error -> productName + "：暂不可用");

            itemFutures.add(itemFuture);
        }

        CompletableFuture<?>[] futureArray =
                itemFutures.toArray(new CompletableFuture<?>[itemFutures.size()]);

        return CompletableFuture.allOf(futureArray)
                .thenApply(ignored -> itemFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }
}
