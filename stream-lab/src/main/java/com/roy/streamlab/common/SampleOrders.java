package com.roy.streamlab.common;

import com.roy.streamlab.model.Order;

import java.util.Arrays;
import java.util.List;

public final class SampleOrders {
    private SampleOrders() {
    }

    public static List<Order> create() {
        return Arrays.asList(
                new Order(
                        "O-1001", "小明", "上海", 29_900, true,
                        Arrays.asList("机械键盘", "无线鼠标")),
                new Order(
                        "O-1002", "小红", "北京", 15_900, true,
                        Arrays.asList("耳机", "无线鼠标")),
                new Order(
                        "O-1003", "小明", "上海", 9_900, false,
                        Arrays.asList("鼠标垫")),
                new Order(
                        "O-1004", "小刚", "深圳", 49_900, true,
                        Arrays.asList("4K显示器", "机械键盘")),
                new Order(
                        "O-1005", "小红", "北京", 8_900, true,
                        Arrays.asList("机械键盘"))
        );
    }
}
