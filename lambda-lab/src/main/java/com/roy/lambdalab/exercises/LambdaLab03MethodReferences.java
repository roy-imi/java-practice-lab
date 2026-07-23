package com.roy.lambdalab.exercises;

import com.roy.lambdalab.model.Product;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/*
 * 第 3 课：方法引用。
 *
 * 【方法引用是什么】
 *
 * 当 Lambda 只是把参数原样转交给一个已有方法时，可以使用 方法引用 简化。
 * 方法引用仍然必须匹配某个函数式接口；它不会立刻调用方法。
 *
 * 【四种常见形式】
 *
 * 1. 类名::静态方法
 *    例如 Integer::parseInt。
 *
 * 2. 已有对象::实例方法
 *    例如 output::accept 或 list::add。
 *    对象已经确定，Lambda 参数会传给这个对象的方法。
 *
 * 3. 类名::实例方法
 *    例如 Product::getName。
 *    Lambda 的第一个参数会成为调用方法的对象。
 *
 * 4. 类名::new
 *    例如 ArrayList::new。
 *    它引用构造器，参数数量必须与目标构造器匹配。
 *
 * 【API 详解：Comparator.compare(first, second)】
 *
 * Comparator 接收两个同类型值并返回 int：
 *
 * - 负数：first 应排在 second 前面。
 * - 0：两者在当前排序规则下相等。
 * - 正数：first 应排在 second 后面。
 *
 * String.compareToIgnoreCase 正好符合“两个 String 输入、一个 int 输出”的形状。
 *
 * 【API 详解：Function.apply(product)】
 *
 * Product::getName 可以匹配 Function：
 *
 * - 输入参数 product 成为调用 getName() 的对象。
 * - getName() 的返回值成为 Function 的返回值。
 *
 * 【API 详解：Supplier.get()】
 *
 * Supplier 无参数并返回一个值。ArrayList::new 匹配无参数构造器，
 * 每次调用 supplier.get() 都应创建一个新的列表。
 */
public final class LambdaLab03MethodReferences {
    private LambdaLab03MethodReferences() {
    }

    public static Comparator<String> caseInsensitiveComparator() {
        /*
         * TODO：使用 String 的实例方法引用，返回忽略大小写的 Comparator。
         */
        throw new UnsupportedOperationException("TODO: 完成 Lambda 第 3 课的 Comparator");
    }

    public static Function<Product, String> nameExtractor() {
        /*
         * TODO：使用 Product 的 getter 方法引用，返回商品名称提取器。
         */
        throw new UnsupportedOperationException("TODO: 完成 Lambda 第 3 课的 Function");
    }

    public static Consumer<String> listAppender(List<String> target) {
        /*
         * TODO：使用已有 target 对象的方法引用，把字符串追加到列表。
         */
        throw new UnsupportedOperationException("TODO: 完成 Lambda 第 3 课的 Consumer");
    }

    public static Supplier<List<String>> listFactory() {
        /*
         * TODO：使用 ArrayList 构造器引用，每次 get() 创建一个新列表。
         */
        throw new UnsupportedOperationException("TODO: 完成 Lambda 第 3 课的 Supplier");
    }
}
