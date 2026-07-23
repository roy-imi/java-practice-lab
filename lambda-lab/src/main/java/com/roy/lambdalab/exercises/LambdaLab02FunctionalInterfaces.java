package com.roy.lambdalab.exercises;

import com.roy.lambdalab.common.Money;
import com.roy.lambdalab.model.Product;

import java.util.function.Function;
import java.util.function.Predicate;

/*
 * 第 2 课：Predicate 和 Function。
 *
 * 课前文档：docs/guides/FUNCTIONAL_INTERFACES_AND_STREAM_GUIDE.md
 * 练习重点：用 Predicate 表达判断规则，用 Function 完成数据转换。
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
        return product -> product.getStock() > 0
                && product.getPriceCents() < maxPriceCents;
    }

    public static Function<Product, String> displayLabel() {
        /*
         * TODO：
         * - 返回 Function Lambda；
         * - 使用 Product 的 getter 取得名称、价格和库存；
         * - Money.format(priceCents) 可以把分格式化为人民币；
         * - 目标格式：“机械键盘 | ¥199.00 | 库存12”。
         */
        return product -> product.getName()
                + "|" + Money.format(product.getPriceCents())
                + "|" + product.getStock();
    }
}
