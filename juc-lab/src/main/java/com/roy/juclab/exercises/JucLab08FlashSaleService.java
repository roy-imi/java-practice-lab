package com.roy.juclab.exercises;

import com.roy.juclab.model.PurchaseResult;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Lab 08：组合并发工具实现单 JVM 秒杀核心。
 *
 * 课前文档：docs/guides/JUC_CONCURRENCY_GUIDE.md（Lab 08）
 * 练习重点：请求幂等、ConcurrentHashMap.computeIfAbsent、CAS 和防超卖。
 */
public final class JucLab08FlashSaleService {
    private final AtomicInteger remainingStock;
    private final ConcurrentMap<String, PurchaseResult> results =
            new ConcurrentHashMap<>();

    public JucLab08FlashSaleService(int initialStock) {
        if (initialStock < 0) {
            throw new IllegalArgumentException("initialStock 不能小于 0");
        }
        this.remainingStock = new AtomicInteger(initialStock);
    }

    public PurchaseResult purchase(String requestId) {
        /*
         * TODO：
         * - requestId 不能为 null；
         * - 使用 computeIfAbsent 确保相同请求返回同一个业务结果；
         * - 第一次出现的 requestId 交给 reserveOne 处理。
         */
        Objects.requireNonNull(requestId, "requestId");
        throw new UnsupportedOperationException("TODO: 完成 JUC Lab 08 的幂等入口");
    }

    private PurchaseResult reserveOne(String requestId) {
        /*
         * TODO：使用 CAS 循环扣减一件库存。
         * 库存为 0 返回 soldOut；扣减成功返回 success。
         */
        throw new UnsupportedOperationException("TODO: 完成 JUC Lab 08 的库存预占");
    }

    public int getRemainingStock() {
        /*
         * TODO：返回剩余库存。
         */
        throw new UnsupportedOperationException("TODO: 完成 JUC Lab 08 的库存读取");
    }

    public int getRecordedRequestCount() {
        /*
         * TODO：返回已经记录业务结果的不同 requestId 数量。
         */
        throw new UnsupportedOperationException("TODO: 完成 JUC Lab 08 的请求计数");
    }
}
