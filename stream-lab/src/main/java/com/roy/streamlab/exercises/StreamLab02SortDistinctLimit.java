package com.roy.streamlab.exercises;

import com.roy.streamlab.model.Order;
import com.sun.org.apache.xpath.internal.operations.Or;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Lab 02：排序、去重和截断。
 *
 * 课前文档：docs/guides/STREAM_PROGRAMMING_GUIDE.md（Lab 02）
 * 练习重点：sorted、map、distinct、limit，以及操作顺序对结果的影响。
 */
public final class StreamLab02SortDistinctLimit {
    private StreamLab02SortDistinctLimit() {
    }

    public static List<String> topPayingCustomers(List<Order> orders,
                                                  int limit) {
        /*
         * TODO：
         * - 只处理已支付订单；
         * - 按 amountCents 从高到低排序；
         * - map：把 Order 转换成 customer；
         * - distinct：同一客户只保留第一次出现；
         * - limit：最多保留 limit 位客户；
         * - collect：收集为 List<String>。
         */
        return orders.stream()
                .filter(Order::isPaid)
                .sorted(Comparator.comparingInt(Order::getAmountCents).reversed())
                .map(Order::getCustomer)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }
}
