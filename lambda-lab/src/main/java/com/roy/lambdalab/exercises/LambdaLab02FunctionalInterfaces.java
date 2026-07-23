package com.roy.lambdalab.exercises;

import com.roy.lambdalab.model.Product;

import java.util.function.Function;
import java.util.function.Predicate;

/*
 * 第 2 课：Predicate 和 Function。
 *
 * 【函数式接口是什么】
 *
 * 函数式接口只有一个抽象方法。Lambda 没有独立类型，必须根据接收它的变量、
 * 方法参数或返回类型，推断自己正在实现哪个函数式接口。
 *
 * 【API 详解：Predicate】
 *
 * Predicate 表示“接收一个值，回答 true 或 false”，唯一抽象方法是 test(value)。
 *
 * - value：要检查的对象，本课中是 Product。
 * - 返回值：boolean，true 表示符合条件，false 表示不符合。
 * - Lambda 形状：product -> 一个 boolean 表达式。
 * - 使用方式：predicate.test(product)。
 *
 * Predicate 常用于校验、筛选和业务规则。它还提供：
 *
 * - first.and(second)：两个条件都为 true。
 * - first.or(second)：至少一个条件为 true。
 * - first.negate()：把条件结果取反。
 *
 * 【API 详解：Function】
 *
 * Function 表示“把一种值转换为另一种值”，唯一抽象方法是 apply(value)。
 *
 * - value：输入值，本课中是 Product。
 * - 返回值：转换后的值，本课中是用于展示的 String。
 * - Lambda 形状：product -> 根据 product 计算一个新值。
 * - 使用方式：function.apply(product)。
 *
 * Function 不应该偷偷修改输入对象；把它理解为输入到输出的映射通常更清晰。
 *
 * 【本课场景】
 *
 * affordableAndInStock 同时判断库存大于 0、价格不超过预算。
 * displayLabel 把 Product 转成“商品名 | ¥金额 | 库存N”的展示文本。
 */
public final class LambdaLab02FunctionalInterfaces {
    private LambdaLab02FunctionalInterfaces() {
    }

    public static Predicate<Product> affordableAndInStock(int maxPriceCents) {
        /*
         * TODO：
         * - 返回 Predicate Lambda；
         * - 参数是一件 Product；
         * - 库存大于 0 并且价格小于等于 maxPriceCents 时返回 true。
         */
        throw new UnsupportedOperationException("TODO: 完成 Lambda 第 2 课的 Predicate");
    }

    public static Function<Product, String> displayLabel() {
        /*
         * TODO：
         * - 返回 Function Lambda；
         * - 使用 Product 的 getter 取得名称、价格和库存；
         * - Money.format(priceCents) 可以把分格式化为人民币；
         * - 目标格式：“机械键盘 | ¥199.00 | 库存12”。
         */
        throw new UnsupportedOperationException("TODO: 完成 Lambda 第 2 课的 Function");
    }
}
