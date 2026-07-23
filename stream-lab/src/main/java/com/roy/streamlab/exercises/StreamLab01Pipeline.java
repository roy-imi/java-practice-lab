package com.roy.streamlab.exercises;

import com.roy.streamlab.model.Order;

import java.util.List;
import java.util.stream.Collectors;

/*
 * Lab 01：基础 Stream 流水线。
 *
 * 课前文档：docs/guides/STREAM_PROGRAMMING_GUIDE.md（Lab 01）
 * 练习重点：stream、filter、map、collect，以及元素类型的变化。
 */
public final class StreamLab01Pipeline {
    private StreamLab01Pipeline() {
    }

    public static List<String> paidOrderIds(List<Order> orders) {
        /*
         * TODO：
         * - 从 orders.stream() 开始；
         * - filter：只保留 isPaid() 为 true 的订单；
         * - map：把 Order 转换成订单 id；
         * - collect：收集为 List<String>；
         * - 保持原订单顺序。
         */
        return orders.stream()
                .filter(Order::isPaid)
                .map(Order::getId)
                .collect(Collectors.toList());
    }
}
