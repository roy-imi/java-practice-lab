package com.roy.streamlab;

import com.roy.streamlab.common.SampleOrders;
import com.roy.streamlab.exercises.StreamLab01Pipeline;
import com.roy.streamlab.exercises.StreamLab02SortDistinctLimit;
import com.roy.streamlab.exercises.StreamLab03FlatMap;
import com.roy.streamlab.exercises.StreamLab04TerminalOperations;
import com.roy.streamlab.exercises.StreamLab05Collectors;
import com.roy.streamlab.model.Order;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class StreamExerciseCheck {
    private StreamExerciseCheck() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                    "用法：StreamExerciseCheck <1-5>");
        }

        int lesson = Integer.parseInt(args[0]);
        try {
            check(lesson);
            System.out.println("[通过] Stream 第 " + lesson + " 课符合预期。");
        } catch (UnsupportedOperationException todo) {
            System.out.println("[未完成] " + todo.getMessage());
            System.exit(1);
        } catch (AssertionError wrong) {
            System.out.println("[未通过] " + wrong.getMessage());
            System.exit(1);
        }
    }

    private static void check(int lesson) {
        switch (lesson) {
            case 1:
                checkLab01();
                return;
            case 2:
                checkLab02();
                return;
            case 3:
                checkLab03();
                return;
            case 4:
                checkLab04();
                return;
            case 5:
                checkLab05();
                return;
            default:
                throw new IllegalArgumentException("课次只能是 1 到 5");
        }
    }

    private static void checkLab01() {
        assertEquals(
                Arrays.asList("O-1001", "O-1002", "O-1004", "O-1005"),
                StreamLab01Pipeline.paidOrderIds(SampleOrders.create()),
                "已支付订单的筛选、映射或顺序错误");
    }

    private static void checkLab02() {
        assertEquals(
                Arrays.asList("小刚", "小明"),
                StreamLab02SortDistinctLimit.topPayingCustomers(
                        SampleOrders.create(), 2),
                "排序、去重或截取结果错误");
    }

    private static void checkLab03() {
        assertEquals(
                Arrays.asList(
                        "机械键盘", "无线鼠标", "耳机", "鼠标垫", "4K显示器"),
                StreamLab03FlatMap.uniqueItemNames(SampleOrders.create()),
                "扁平化、去重或元素顺序错误");
    }

    private static void checkLab04() {
        List<Order> orders = SampleOrders.create();
        assertEquals(
                104_600,
                StreamLab04TerminalOperations.totalPaidAmountCents(orders),
                "已支付金额求和错误");
        assertTrue(
                StreamLab04TerminalOperations.hasUnpaidOrder(orders),
                "应检测到未支付订单");

        Optional<Order> first =
                StreamLab04TerminalOperations
                        .findFirstOrderByCustomer(orders, "小红");
        assertTrue(first.isPresent(), "应找到小红的订单");
        assertEquals("O-1002", first.get().getId(), "第一笔订单错误");
        assertTrue(
                !StreamLab04TerminalOperations
                        .findFirstOrderByCustomer(orders, "不存在")
                        .isPresent(),
                "不存在的客户应该返回 Optional.empty()");
    }

    private static void checkLab05() {
        List<Order> orders = SampleOrders.create();
        Map<String, Integer> amountByCity =
                StreamLab05Collectors.paidAmountByCity(orders);
        assertEquals(3, amountByCity.size(), "城市分组数量错误");
        assertEquals(29_900, amountByCity.get("上海"), "上海金额错误");
        assertEquals(24_800, amountByCity.get("北京"), "北京金额错误");
        assertEquals(49_900, amountByCity.get("深圳"), "深圳金额错误");

        Map<Boolean, List<Order>> partition =
                StreamLab05Collectors.partitionByHighValue(orders, 20_000);
        assertEquals(
                Arrays.asList("O-1001", "O-1004"),
                orderIds(partition.get(true)),
                "高价值订单分区错误");
        assertEquals(
                Arrays.asList("O-1002", "O-1003", "O-1005"),
                orderIds(partition.get(false)),
                "普通订单分区错误");
    }

    private static List<String> orderIds(List<Order> orders) {
        return orders.stream()
                .map(Order::getId)
                .collect(Collectors.toList());
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected,
                                     Object actual,
                                     String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(
                    message + "；期望 " + expected + "，实际 " + actual);
        }
    }
}
