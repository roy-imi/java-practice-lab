package com.roy.streamlab.answers;

import com.roy.streamlab.model.Order;

import java.util.List;
import java.util.stream.Collectors;

public final class StreamLab03FlatMapAnswer {
    private StreamLab03FlatMapAnswer() {
    }

    public static List<String> uniqueItemNames(List<Order> orders) {
        return orders.stream()
                .flatMap(order -> order.getItemNames().stream())
                .distinct()
                .collect(Collectors.toList());
    }
}
