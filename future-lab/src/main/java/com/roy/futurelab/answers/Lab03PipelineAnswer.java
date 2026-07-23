package com.roy.futurelab.answers;

import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.common.Money;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public final class Lab03PipelineAnswer {
    private Lab03PipelineAnswer() {
    }

    public static CompletableFuture<String> buildVipPriceLabel(DemoServices services,
                                                               Executor executor,
                                                               String productName) {
        return CompletableFuture
                .supplyAsync(() -> services.queryPriceCents(productName), executor)
                .thenApply(priceCents -> priceCents * 90 / 100)
                .thenApply(vipPriceCents -> "VIP价：" + Money.format(vipPriceCents));
    }

    public static CompletableFuture<Void> notifyWhenReady(CompletableFuture<String> labelFuture,
                                                          Consumer<String> notifier) {
        return labelFuture.thenAccept(notifier);
    }
}
