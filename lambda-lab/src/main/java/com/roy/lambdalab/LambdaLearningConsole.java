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
import java.util.function.Function;
import java.util.function.Predicate;

public final class LambdaLearningConsole {
    private LambdaLearningConsole() {
    }

    public static void main(String[] args) {
        int lesson = args.length == 0 ? 1 : Integer.parseInt(args[0]);
        System.out.println("=== 运行 Lambda 第 " + lesson + " 课 ===");

        try {
            runLesson(lesson);
        } catch (UnsupportedOperationException todo) {
            System.out.println(todo.getMessage());
            System.out.println("需要时查看 docs/hints/lab-0" + lesson + ".md");
        }
    }

    private static void runLesson(int lesson) {
        switch (lesson) {
            case 1:
                int discounted = LambdaLab01Syntax.discountCalculator()
                        .applyAsInt(19_900, 10);
                System.out.println("九折价格（分）：" + discounted);
                LambdaLab01Syntax.greetingTask(
                        "Lambda 学习者", System.out::println).run();
                return;
            case 2:
                Predicate<Product> rule =
                        LambdaLab02FunctionalInterfaces.affordableAndInStock(20_000);
                Function<Product, String> label =
                        LambdaLab02FunctionalInterfaces.displayLabel();
                SampleProducts.create().stream()
                        .filter(rule)
                        .map(label)
                        .forEach(System.out::println);
                return;
            case 3:
                List<String> words =
                        new ArrayList<>(Arrays.asList("beta", "Alpha", "gamma"));
                words.sort(LambdaLab03MethodReferences.caseInsensitiveComparator());
                System.out.println("忽略大小写排序：" + words);
                System.out.println("提取名称："
                        + LambdaLab03MethodReferences.nameExtractor()
                        .apply(SampleProducts.create().get(0)));
                List<String> target =
                        LambdaLab03MethodReferences.listFactory().get();
                LambdaLab03MethodReferences.listAppender(target).accept("已追加");
                System.out.println(target);
                return;
            case 4:
                System.out.println(LambdaLab04Streams.findAffordableProductNames(
                        SampleProducts.create(), 20_000));
                return;
            default:
                throw new IllegalArgumentException("课次只能是 1 到 4");
        }
    }
}
