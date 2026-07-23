package com.roy.lambdalab.exercises;

import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;

/*
 * 第 1 课：Lambda 基本语法。
 *
 * 【先理解 Lambda 的组成】
 *
 * Lambda 的结构是：
 *
 * (参数列表) -> 表达式或代码块
 *
 * - 左边说明函数接收哪些参数。
 * - -> 可以读作“根据这些参数，执行右边的逻辑”。
 * - 右边只有一个表达式时，表达式结果会自动作为返回值。
 * - 右边有多条语句时必须使用大括号；需要返回值时还要显式写 return。
 *
 * 【为什么有时写空括号】
 *
 * () 表示函数不接收参数。例如 Runnable.run() 没有参数，所以对应 Lambda
 * 必须以 () 开头。一个参数时可以写 value -> ...；两个参数时写 (a, b) -> ...。
 *
 * 【API 详解：IntBinaryOperator】
 *
 * IntBinaryOperator 表示“接收两个 int，返回一个 int”的函数式接口。
 * 它唯一的抽象方法是 applyAsInt(left, right)。
 *
 * - left：第一个 int 参数，本题约定为商品原价，单位是分。
 * - right：第二个 int 参数，本题约定为折扣百分比，例如 10 表示减免 10%。
 * - 返回值：计算后的价格，仍然使用分。
 * - 使用方式：operator.applyAsInt(19900, 10)。
 *
 * 选择 IntBinaryOperator 而不是 BiFunction 的原因是：它直接处理基本类型 int，
 * 可以避免 Integer 的装箱和拆箱。
 *
 * 【API 详解：Runnable】
 *
 * Runnable 表示“无参数、无返回值”的动作，唯一抽象方法是 run()。
 *
 * - Lambda 参数列表：必须是空括号 ()。
 * - Lambda 返回值：没有返回值。
 * - 使用方式：先得到 Runnable，再调用 runnable.run() 执行动作。
 *
 * 【API 详解：Consumer.accept(value)】
 *
 * Consumer 表示“接收一个值，但不返回结果”的操作。
 *
 * - value：交给 Consumer 处理的值，本题中是一条问候字符串。
 * - 返回值：没有返回值。
 * - output 由调用方传入，因此 Lambda 只决定消息内容，不绑定具体输出位置。
 *
 * 【变量捕获】
 *
 * greetingTask 方法的 Lambda 需要使用外层的 name 和 output。
 * 这种行为叫捕获外层变量。被捕获的局部变量必须是 final 或事实上不再被重新赋值。
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
        throw new UnsupportedOperationException("TODO: 完成 Lambda 第 1 课的计算器");
    }

    public static Runnable greetingTask(String name, Consumer<String> output) {
        /*
         * TODO：
         * - 返回一个无参数 Lambda；
         * - Lambda 执行时调用 output.accept(...)；
         * - 消息格式为“你好，名字！”；
         * - 不要现在就调用 output，动作应等到 Runnable.run() 时才发生。
         */
        throw new UnsupportedOperationException("TODO: 完成 Lambda 第 1 课的 Runnable");
    }
}
