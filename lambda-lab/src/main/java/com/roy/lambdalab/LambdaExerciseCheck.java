package com.roy.lambdalab;

import com.roy.lambdalab.common.SampleProducts;
import com.roy.lambdalab.exercises.LambdaLab01Syntax;
import com.roy.lambdalab.exercises.LambdaLab02FunctionalInterfaces;
import com.roy.lambdalab.exercises.LambdaLab03MethodReferences;
import com.roy.lambdalab.exercises.LambdaLab04Streams;
import com.roy.lambdalab.model.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public final class LambdaExerciseCheck {
    private LambdaExerciseCheck() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                    "用法：LambdaExerciseCheck <1-4>");
        }

        int lesson = Integer.parseInt(args[0]);
        try {
            check(lesson);
            System.out.println("[通过] Lambda 第 " + lesson + " 课符合预期。");
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
            default:
                throw new IllegalArgumentException("课次只能是 1 到 4");
        }
    }

    private static void checkLab01() {
        assertEquals(
                17_910,
                LambdaLab01Syntax.discountCalculator().applyAsInt(19_900, 10),
                "折扣计算错误");

        AtomicReference<String> message = new AtomicReference<>();
        Runnable task = LambdaLab01Syntax.greetingTask("小明", message::set);
        assertEquals(null, message.get(), "创建 Runnable 时不应立即执行");
        task.run();
        assertEquals("你好，小明！", message.get(), "问候内容错误");
    }

    private static void checkLab02() {
        List<Product> products = SampleProducts.create();
        Predicate<Product> rule =
                LambdaLab02FunctionalInterfaces.affordableAndInStock(20_000);
        assertTrue(rule.test(products.get(0)), "机械键盘应满足条件");
        assertTrue(!rule.test(products.get(2)), "无库存耳机不应满足条件");
        assertTrue(!rule.test(products.get(3)), "超预算显示器不应满足条件");

        String label = LambdaLab02FunctionalInterfaces.displayLabel()
                .apply(products.get(0));
        assertEquals(
                "机械键盘 | ¥199.00 | 库存12",
                label,
                "展示文本错误");
    }

    private static void checkLab03() {
        List<String> words =
                new ArrayList<>(Arrays.asList("beta", "Alpha", "gamma"));
        words.sort(LambdaLab03MethodReferences.caseInsensitiveComparator());
        assertEquals(
                Arrays.asList("Alpha", "beta", "gamma"),
                words,
                "Comparator 错误");

        assertEquals(
                "机械键盘",
                LambdaLab03MethodReferences.nameExtractor()
                        .apply(SampleProducts.create().get(0)),
                "名称提取错误");

        List<String> target = new ArrayList<>();
        LambdaLab03MethodReferences.listAppender(target).accept("消息");
        assertEquals(Arrays.asList("消息"), target, "列表追加错误");

        List<String> first = LambdaLab03MethodReferences.listFactory().get();
        List<String> second = LambdaLab03MethodReferences.listFactory().get();
        assertTrue(first != second, "Supplier 每次应创建新列表");
    }

    private static void checkLab04() {
        List<String> result = LambdaLab04Streams.findAffordableProductNames(
                SampleProducts.create(), 20_000);
        assertEquals(
                Arrays.asList("无线鼠标", "机械键盘"),
                result,
                "筛选、排序或映射结果错误");
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
