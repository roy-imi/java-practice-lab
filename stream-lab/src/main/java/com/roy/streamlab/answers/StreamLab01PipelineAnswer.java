package com.roy.streamlab.answers;

import com.roy.streamlab.model.Order;

import java.util.List;
import java.util.stream.Collectors;

public final class StreamLab01PipelineAnswer {
    private StreamLab01PipelineAnswer() {
    }

    public static List<String> paidOrderIds(List<Order> orders) {
        return orders.stream()
                .filter(Order::isPaid)
                .map(Order::getId)
                .collect(Collectors.toList());
    }
}
