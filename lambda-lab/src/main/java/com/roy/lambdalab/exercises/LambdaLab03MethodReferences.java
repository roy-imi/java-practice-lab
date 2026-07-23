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
 * 课前文档：
 * - docs/guides/LAMBDA_EXPRESSION_GUIDE.md
 * - docs/guides/FUNCTIONAL_INTERFACES_AND_STREAM_GUIDE.md
 *
 * 练习重点：识别 Lambda 只转发已有方法的情况，并改写为方法引用。
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
