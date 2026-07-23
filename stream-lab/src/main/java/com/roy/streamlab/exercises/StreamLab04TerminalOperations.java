package com.roy.streamlab.exercises;

import com.roy.streamlab.model.Order;
import com.sun.org.apache.xpath.internal.operations.Or;

import java.util.List;
import java.util.Optional;

/*
 * Lab 04：终止操作、数值流和 Optional。
 *
 * 课前文档：docs/guides/STREAM_PROGRAMMING_GUIDE.md（Lab 04）
 * 练习重点：mapToInt、sum、anyMatch、findFirst，以及安全处理 Optional。
 */
public final class StreamLab04TerminalOperations {
    private StreamLab04TerminalOperations() {
    }

    public static int totalPaidAmountCents(List<Order> orders) {
        /*
         * TODO：筛选已支付订单，转成 IntStream，并对 amountCents 求和。
         */
        return orders.stream()
                .filter(Order::isPaid)
                .mapToInt(Order::getAmountCents)
                .sum();
    }

    public static boolean hasUnpaidOrder(List<Order> orders) {
        /*
         * TODO：使用 anyMatch 判断是否存在未支付订单。
         */
        return orders.stream()
                .anyMatch(order -> !order.isPaid());
    }

    public static Optional<Order> findFirstOrderByCustomer(List<Order> orders,
                                                            String customer) {
        /*
         * TODO：筛选指定客户，使用 findFirst 返回 Optional<Order>。
         * 不要在这个方法中直接调用 Optional.get()。
         */
        return orders.stream()
                .filter(order -> order.getCustomer().equals(customer))
                .findFirst();
    }
}
