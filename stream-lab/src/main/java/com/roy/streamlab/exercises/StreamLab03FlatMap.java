package com.roy.streamlab.exercises;

import com.roy.streamlab.model.Order;

import java.util.List;
import java.util.stream.Collectors;

/*
 * Lab 03：使用 flatMap 扁平化嵌套集合。
 *
 * 课前文档：docs/guides/STREAM_PROGRAMMING_GUIDE.md（Lab 03）
 * 练习重点：map 与 flatMap 的区别，以及嵌套列表如何变成单层元素流。
 */
public final class StreamLab03FlatMap {
    private StreamLab03FlatMap() {
    }

    public static List<String> uniqueItemNames(List<Order> orders) {
        /*
         * TODO：
         * - 从订单流开始；
         * - 取出每个订单的 itemNames；
         * - 用 flatMap 把多个 List<String> 展开成一个 Stream<String>；
         * - distinct：商品名去重；
         * - collect：按第一次出现的顺序收集。
         */
        return orders.stream()
                .flatMap(order -> order.getItemNames().stream())
                .distinct()
                .collect(Collectors.toList());
    }
}
