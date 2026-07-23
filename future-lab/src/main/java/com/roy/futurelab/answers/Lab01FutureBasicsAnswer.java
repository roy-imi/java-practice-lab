package com.roy.futurelab.answers;

import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.model.ProductSummary;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public final class Lab01FutureBasicsAnswer {
    private Lab01FutureBasicsAnswer() {
    }

    public static ProductSummary loadProductSummary(DemoServices services,
                                                    ExecutorService executor,
                                                    String productName,
                                                    Consumer<String> trace)
            throws Exception {
        Future<Integer> priceFuture =
                executor.submit(() -> services.queryPriceCents(productName));
        Future<Integer> stockFuture =
                executor.submit(() -> services.queryStock(productName));

        trace.accept("刚提交：price.isDone=" + priceFuture.isDone()
                + ", stock.isDone=" + stockFuture.isDone());

        int priceCents = priceFuture.get();
        int stock = stockFuture.get();
        return new ProductSummary(productName, priceCents, stock);
    }
}
