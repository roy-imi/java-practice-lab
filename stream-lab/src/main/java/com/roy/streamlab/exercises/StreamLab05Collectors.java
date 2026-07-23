package com.roy.streamlab.exercises;

import com.roy.streamlab.model.Order;
import com.sun.org.apache.xpath.internal.operations.Or;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/*
 * Lab 05：Collectors 分组、聚合和分区。
 *
 * 课前文档：docs/guides/STREAM_PROGRAMMING_GUIDE.md（Lab 05）
 * 练习重点：groupingBy、summingInt、partitioningBy 和下游 Collector。
 */
public final class StreamLab05Collectors {
    private StreamLab05Collectors() {
    }

    public static Map<String, Integer> paidAmountByCity(List<Order> orders) {
        /*
         * TODO：
         * - 只保留已支付订单；
         * - groupingBy：按 city 分组；
         * - summingInt：汇总每个城市的 amountCents。
         */
        return orders.stream()
                .filter(Order::isPaid)
                .collect(
                        Collectors.groupingBy(
                                Order::getCity,
                                Collectors.summingInt(Order::getAmountCents))
                        );
    }

    public static Map<Boolean, List<Order>> partitionByHighValue(
            List<Order> orders,
            int thresholdCents) {
        /*
         * TODO：使用 partitioningBy 按 amountCents >= thresholdCents 分成两组。
         */
        return orders.stream()
                .collect(
                        Collectors.partitioningBy(order -> order.getAmountCents() >= thresholdCents)
                );
    }
}
