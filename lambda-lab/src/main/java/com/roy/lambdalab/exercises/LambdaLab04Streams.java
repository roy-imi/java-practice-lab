package com.roy.lambdalab.exercises;

import com.roy.lambdalab.model.Product;

import java.util.List;

/*
 * 第 4 课：在 Stream 中使用 Lambda。
 *
 * 【Stream 是什么】
 *
 * Stream 描述一条数据处理流水线。中间操作只描述规则，终止操作触发整个流水线。
 * Stream 不会修改原 List，也不能重复消费同一个 Stream 实例。
 *
 * 本课目标：
 *
 * 商品列表
 * -> 保留有库存且价格不超过预算的商品
 * -> 按价格从低到高排序
 * -> 转换成商品名称
 * -> 收集为新的 List
 *
 * 【API 详解：products.stream()】
 *
 * - 参数：没有参数。
 * - 返回值：按列表遍历顺序产生元素的 Stream。
 * - 注意：此时通常还没有开始逐个处理元素。
 *
 * 【API 详解：stream.filter(predicate)】
 *
 * - predicate：接收一个元素并返回 boolean。
 * - true：元素保留；false：元素丢弃。
 * - 返回值：新的 Stream，元素类型不变。
 * - Lambda 形状：product -> 条件表达式。
 *
 * 【API 详解：stream.sorted(comparator)】
 *
 * - comparator：接收两个元素，决定它们的先后顺序。
 * - 返回值：按规则排序后的新 Stream。
 * - 本课可使用 Comparator.comparingInt(keyExtractor)：
 *   keyExtractor 接收 Product，返回用于排序的 int 价格。
 *
 * 【API 详解：stream.map(mapper)】
 *
 * - mapper：Function，把当前元素转换成另一个值。
 * - 返回值：元素类型可以发生变化的新 Stream。
 * - 本课中输入是 Product，输出是商品名称 String。
 *
 * 【API 详解：stream.collect(collector)】
 *
 * - collector：描述如何汇总 Stream 元素。
 * - Collectors.toList()：把所有元素收集到一个 List。
 * - collect 是终止操作，会真正触发前面的 filter、sorted 和 map。
 *
 * 【操作顺序为什么重要】
 *
 * 先 filter 再 sorted，可以减少需要排序的元素数量。
 * 先 sorted 再 map，可以继续使用 Product 的价格作为排序依据。
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
