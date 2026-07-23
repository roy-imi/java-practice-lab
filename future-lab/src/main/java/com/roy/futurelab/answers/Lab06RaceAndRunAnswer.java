package com.roy.futurelab.answers;

import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.model.ShopQuote;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public final class Lab06RaceAndRunAnswer {
    private Lab06RaceAndRunAnswer() {
    }

    public static CompletableFuture<ShopQuote> firstCompletedQuote(DemoServices services,
                                                                  Executor executor,
                                                                  List<String> shopNames) {
        if (shopNames.isEmpty()) {
            throw new IllegalArgumentException("至少需要一家商店");
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
        return CompletableFuture.runAsync(
                () -> auditWriter.accept("采用报价：" + quote),
                executor);
    }
}
