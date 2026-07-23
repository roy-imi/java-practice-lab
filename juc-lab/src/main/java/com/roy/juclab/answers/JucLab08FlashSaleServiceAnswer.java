package com.roy.juclab.answers;

import com.roy.juclab.model.PurchaseResult;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class JucLab08FlashSaleServiceAnswer {
    private final AtomicInteger remainingStock;
    private final ConcurrentMap<String, PurchaseResult> results =
            new ConcurrentHashMap<>();

    public JucLab08FlashSaleServiceAnswer(int initialStock) {
        if (initialStock < 0) {
            throw new IllegalArgumentException("initialStock 不能小于 0");
        }
        this.remainingStock = new AtomicInteger(initialStock);
    }

    public PurchaseResult purchase(String requestId) {
        Objects.requireNonNull(requestId, "requestId");
        return results.computeIfAbsent(requestId, this::reserveOne);
    }

    private PurchaseResult reserveOne(String requestId) {
        while (true) {
            int current = remainingStock.get();
            if (current == 0) {
                return PurchaseResult.soldOut(requestId);
            }
            if (remainingStock.compareAndSet(current, current - 1)) {
                return PurchaseResult.success(requestId);
            }
        }
    }

    public int getRemainingStock() {
        return remainingStock.get();
    }

    public int getRecordedRequestCount() {
        return results.size();
    }
}
