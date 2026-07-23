package com.roy.lambdalab.exercises;

import com.roy.lambdalab.model.Product;

import java.util.List;

/*
 * 第 4 课：在 Stream 中使用 Lambda。
 *
 * 课前文档：docs/guides/FUNCTIONAL_INTERFACES_AND_STREAM_GUIDE.md
 * 练习重点：filter、sorted、map、collect 以及操作顺序。
 */
public final class LambdaLab04Streams {
    private LambdaLab04Streams() {
    }

    public static List<String> findAffordableProductNames(List<Product> products,
                                                          int maxPriceCents) {
        /*
         * TODO：
         * - 从 products.stream() 开始；
         * - filter：库存大于 0 且价格不超过 maxPriceCents；
         * - sorted：按 priceCents 从低到高；
         * - map：只保留商品名称；
         * - collect：收集成 List 并返回。
         */
        throw new UnsupportedOperationException("TODO: 完成 Lambda 第 4 课");
    }
}
