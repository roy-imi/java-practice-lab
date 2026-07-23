package com.roy.futurelab.answers;

import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.model.CheckoutSummary;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class Lab04ComposeAndCombineAnswer {
    private Lab04ComposeAndCombineAnswer() {
    }

    public static CompletableFuture<CheckoutSummary> buildCheckout(DemoServices services,
                                                                   Executor executor,
                                                                   String productName,
                                                                   String userId,
                                                                   String city) {
        CompletableFuture<Integer> priceFuture = CompletableFuture.supplyAsync(
                () -> services.queryPriceCents(productName), executor);

        CompletableFuture<Integer> deliveryFuture = CompletableFuture.supplyAsync(
                () -> services.queryDeliveryFeeCents(city), executor);

        CompletableFuture<String> userLevelFuture = CompletableFuture.supplyAsync(
                () -> services.queryUserLevel(userId), executor);

        CompletableFuture<Integer> discountFuture = userLevelFuture.thenCompose(
                level -> CompletableFuture.supplyAsync(
                        () -> services.queryDiscountPercent(level), executor));

        CompletableFuture<int[]> priceAndDiscount = priceFuture.thenCombine(
                discountFuture,
                (priceCents, discountPercent) ->
                        new int[]{priceCents, discountPercent});

        return priceAndDiscount.thenCombine(
                deliveryFuture,
                (values, deliveryFeeCents) ->
                        new CheckoutSummary(values[0], values[1], deliveryFeeCents));
    }
}
