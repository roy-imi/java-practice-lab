package com.roy.lambdalab.exercises;

import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;

/*
 * 第 1 课：Lambda 基本语法。
 *
 * 课前文档：docs/guides/LAMBDA_EXPRESSION_GUIDE.md
 * 练习重点：参数列表、表达式体、Runnable、IntBinaryOperator 和变量捕获。
 */
public final class LambdaLab01Syntax {
    private LambdaLab01Syntax() {
    }

    public static IntBinaryOperator discountCalculator() {
        /*
         * TODO：
         * - 返回一个接收 priceCents 和 discountPercent 的 Lambda；
         * - 计算 priceCents * (100 - discountPercent) / 100；
         * - 先判断参数列表应该有几个参数。
         */
        return (priceCents, discountPercent)
                -> priceCents * (100 - discountPercent) / 100;
    }

    public static Runnable greetingTask(String name, Consumer<String> output) {
        /*
         * TODO：
         * - 返回一个无参数 Lambda；
         * - Lambda 执行时调用 output.accept(...)；
         * - 消息格式为“你好，名字！”；
         * - 不要现在就调用 output，动作应等到 Runnable.run() 时才发生。
         */
        return () -> output.accept("你好，"+ name + "！");

    }
}
