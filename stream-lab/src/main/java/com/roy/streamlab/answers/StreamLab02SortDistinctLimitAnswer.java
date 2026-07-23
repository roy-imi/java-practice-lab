package com.roy.streamlab.answers;

import com.roy.streamlab.model.Order;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class StreamLab02SortDistinctLimitAnswer {
    private StreamLab02SortDistinctLimitAnswer() {
    }

    public static List<String> topPayingCustomers(List<Order> orders,
                                                  int limit) {
        return orders.stream()
                .filter(Order::isPaid)
                .sorted(Comparator.comparingInt(Order::getAmountCents)
                        .reversed())
                .map(Order::getCustomer)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }
}
