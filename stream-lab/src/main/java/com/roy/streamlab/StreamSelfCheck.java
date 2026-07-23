package com.roy.streamlab;

import com.roy.streamlab.answers.StreamLab01PipelineAnswer;
import com.roy.streamlab.answers.StreamLab02SortDistinctLimitAnswer;
import com.roy.streamlab.answers.StreamLab03FlatMapAnswer;
import com.roy.streamlab.answers.StreamLab04TerminalOperationsAnswer;
import com.roy.streamlab.answers.StreamLab05CollectorsAnswer;
import com.roy.streamlab.common.SampleOrders;
import com.roy.streamlab.model.Order;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class StreamSelfCheck {
    private StreamSelfCheck() {
    }

    public static void main(String[] args) {
        checkLab01();
        checkLab02();
        checkLab03();
        checkLab04();
        checkLab05();
        System.out.println();
        System.out.println("全部通过：Stream 5/5。");
    }

    private static void checkLab01() {
        assertEquals(
                Arrays.asList("O-1001", "O-1002", "O-1004", "O-1005"),
                StreamLab01PipelineAnswer
                        .paidOrderIds(SampleOrders.create()));
        pass(1, "创建、筛选、映射与收集");
    }

    private static void checkLab02() {
        assertEquals(
                Arrays.asList("小刚", "小明"),
                StreamLab02SortDistinctLimitAnswer
                        .topPayingCustomers(SampleOrders.create(), 2));
        pass(2, "排序、去重与截取");
    }

    private static void checkLab03() {
        assertEquals(
                Arrays.asList(
                        "机械键盘", "无线鼠标", "耳机", "鼠标垫", "4K显示器"),
                StreamLab03FlatMapAnswer
                        .uniqueItemNames(SampleOrders.create()));
        pass(3, "map 与 flatMap");
    }

    private static void checkLab04() {
        List<Order> orders = SampleOrders.create();
        assertEquals(
                104_600,
                StreamLab04TerminalOperationsAnswer
                        .totalPaidAmountCents(orders));
        assertTrue(
                StreamLab04TerminalOperationsAnswer
                        .hasUnpaidOrder(orders));
        Optional<Order> first =
                StreamLab04TerminalOperationsAnswer
                        .findFirstOrderByCustomer(orders, "小红");
        assertTrue(first.isPresent());
        assertEquals("O-1002", first.get().getId());
        pass(4, "终止操作、数值流与 Optional");
    }

    private static void checkLab05() {
        List<Order> orders = SampleOrders.create();
        Map<String, Integer> amountByCity =
                StreamLab05CollectorsAnswer.paidAmountByCity(orders);
        assertEquals(29_900, amountByCity.get("上海"));
        assertEquals(24_800, amountByCity.get("北京"));
        assertEquals(49_900, amountByCity.get("深圳"));

        Map<Boolean, List<Order>> partition =
                StreamLab05CollectorsAnswer
                        .partitionByHighValue(orders, 20_000);
        assertEquals(
                Arrays.asList("O-1001", "O-1004"),
                orderIds(partition.get(true)));
        assertEquals(
                Arrays.asList("O-1002", "O-1003", "O-1005"),
                orderIds(partition.get(false)));
        pass(5, "Collectors 分组、聚合与分区");
    }

    private static List<String> orderIds(List<Order> orders) {
        return orders.stream()
                .map(Order::getId)
                .collect(Collectors.toList());
    }

    private static void pass(int lesson, String topic) {
        System.out.println("[通过] 第 " + lesson + " 课：" + topic);
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("条件不成立");
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(
                    "期望 " + expected + "，实际 " + actual);
        }
    }
}
