package com.roy.streamlab.answers;

import com.roy.streamlab.model.Order;

import java.util.List;
import java.util.Optional;

public final class StreamLab04TerminalOperationsAnswer {
    private StreamLab04TerminalOperationsAnswer() {
    }

    public static int totalPaidAmountCents(List<Order> orders) {
        return orders.stream()
                .filter(Order::isPaid)
                .mapToInt(Order::getAmountCents)
                .sum();
    }

    public static boolean hasUnpaidOrder(List<Order> orders) {
        return orders.stream()
                .anyMatch(order -> !order.isPaid());
    }

    public static Optional<Order> findFirstOrderByCustomer(
            List<Order> orders,
            String customer) {
        return orders.stream()
                .filter(order -> order.getCustomer().equals(customer))
                .findFirst();
    }
}
