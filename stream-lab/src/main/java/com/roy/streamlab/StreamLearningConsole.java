package com.roy.streamlab;

import com.roy.streamlab.common.SampleOrders;
import com.roy.streamlab.exercises.StreamLab01Pipeline;
import com.roy.streamlab.exercises.StreamLab02SortDistinctLimit;
import com.roy.streamlab.exercises.StreamLab03FlatMap;
import com.roy.streamlab.exercises.StreamLab04TerminalOperations;
import com.roy.streamlab.exercises.StreamLab05Collectors;
import com.roy.streamlab.model.Order;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class StreamLearningConsole {
    private StreamLearningConsole() {
    }

    public static void main(String[] args) {
        int lesson = args.length == 0 ? 1 : Integer.parseInt(args[0]);
        System.out.println("=== 运行 Stream 第 " + lesson + " 课 ===");

        try {
            runLesson(lesson);
        } catch (UnsupportedOperationException todo) {
            System.out.println(todo.getMessage());
            System.out.println("需要时查看 docs/hints/lab-0" + lesson + ".md");
        }
    }

    private static void runLesson(int lesson) {
        List<Order> orders = SampleOrders.create();
        switch (lesson) {
            case 1:
                System.out.println("已支付订单编号："
                        + StreamLab01Pipeline.paidOrderIds(orders));
                return;
            case 2:
                System.out.println("支付金额最高的两个客户："
                        + StreamLab02SortDistinctLimit
                        .topPayingCustomers(orders, 2));
                return;
            case 3:
                System.out.println("去重后的商品名称："
                        + StreamLab03FlatMap.uniqueItemNames(orders));
                return;
            case 4:
                System.out.println("已支付总金额（分）："
                        + StreamLab04TerminalOperations
                        .totalPaidAmountCents(orders));
                System.out.println("存在未支付订单："
                        + StreamLab04TerminalOperations
                        .hasUnpaidOrder(orders));
                Optional<Order> first =
                        StreamLab04TerminalOperations
                                .findFirstOrderByCustomer(orders, "小红");
                System.out.println("小红的第一笔订单："
                        + first.map(Order::getId).orElse("未找到"));
                return;
            case 5:
                Map<String, Integer> amountByCity =
                        StreamLab05Collectors.paidAmountByCity(orders);
                System.out.println("各城市已支付金额：" + amountByCity);
                Map<Boolean, List<Order>> partition =
                        StreamLab05Collectors
                                .partitionByHighValue(orders, 20_000);
                System.out.println("高价值订单数量："
                        + partition.get(true).size());
                System.out.println("普通订单数量："
                        + partition.get(false).size());
                return;
            default:
                throw new IllegalArgumentException("课次只能是 1 到 5");
        }
    }
}
