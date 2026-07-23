package com.roy.lambdalab;

import com.roy.lambdalab.answers.LambdaLab01SyntaxAnswer;
import com.roy.lambdalab.answers.LambdaLab02FunctionalInterfacesAnswer;
import com.roy.lambdalab.answers.LambdaLab03MethodReferencesAnswer;
import com.roy.lambdalab.answers.LambdaLab04StreamsAnswer;
import com.roy.lambdalab.common.SampleProducts;
import com.roy.lambdalab.model.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public final class LambdaSelfCheck {
    private LambdaSelfCheck() {
    }

    public static void main(String[] args) {
        checkLab01();
        checkLab02();
        checkLab03();
        checkLab04();
        System.out.println();
        System.out.println("全部通过：Lambda 4/4。");
    }

    private static void checkLab01() {
        assertEquals(
                17_910,
                LambdaLab01SyntaxAnswer.discountCalculator()
                        .applyAsInt(19_900, 10));
        AtomicReference<String> message = new AtomicReference<>();
        LambdaLab01SyntaxAnswer.greetingTask("小明", message::set).run();
        assertEquals("你好，小明！", message.get());
        pass(1, "基本语法与变量捕获");
    }

    private static void checkLab02() {
        List<Product> products = SampleProducts.create();
        Predicate<Product> rule =
                LambdaLab02FunctionalInterfacesAnswer
                        .affordableAndInStock(20_000);
        assertTrue(rule.test(products.get(0)));
        assertTrue(!rule.test(products.get(2)));
        assertEquals(
                "机械键盘 | ¥199.00 | 库存12",
                LambdaLab02FunctionalInterfacesAnswer.displayLabel()
                        .apply(products.get(0)));
        pass(2, "Predicate 与 Function");
    }

    private static void checkLab03() {
        List<String> words =
                new ArrayList<>(Arrays.asList("beta", "Alpha", "gamma"));
        words.sort(
                LambdaLab03MethodReferencesAnswer.caseInsensitiveComparator());
        assertEquals(Arrays.asList("Alpha", "beta", "gamma"), words);

        List<String> target =
                LambdaLab03MethodReferencesAnswer.listFactory().get();
        LambdaLab03MethodReferencesAnswer.listAppender(target).accept("消息");
        assertEquals(Arrays.asList("消息"), target);
        assertEquals(
                "机械键盘",
                LambdaLab03MethodReferencesAnswer.nameExtractor()
                        .apply(SampleProducts.create().get(0)));
        pass(3, "方法引用");
    }

    private static void checkLab04() {
        assertEquals(
                Arrays.asList("无线鼠标", "机械键盘"),
                LambdaLab04StreamsAnswer.findAffordableProductNames(
                        SampleProducts.create(), 20_000));
        pass(4, "Stream 流水线");
    }

    private static void pass(int lesson, String topic) {
        System.out.println("[通过] 第 " + lesson + " 课：" + topic);
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("条件不成立");
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(
                    "期望 " + expected + "，实际 " + actual);
        }
    }
}
