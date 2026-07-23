package com.roy.streamlab.answers;

import com.roy.streamlab.model.Order;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class StreamLab05CollectorsAnswer {
    private StreamLab05CollectorsAnswer() {
    }

    public static Map<String, Integer> paidAmountByCity(List<Order> orders) {
        return orders.stream()
                .filter(Order::isPaid)
                .collect(Collectors.groupingBy(
                        Order::getCity,
                        Collectors.summingInt(Order::getAmountCents)));
    }

    public static Map<Boolean, List<Order>> partitionByHighValue(
            List<Order> orders,
            int thresholdCents) {
        return orders.stream()
                .collect(Collectors.partitioningBy(
                        order -> order.getAmountCents() >= thresholdCents));
    }
}
