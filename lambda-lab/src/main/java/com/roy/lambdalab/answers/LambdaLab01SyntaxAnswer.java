package com.roy.lambdalab.answers;

import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;

public final class LambdaLab01SyntaxAnswer {
    private LambdaLab01SyntaxAnswer() {
    }

    public static IntBinaryOperator discountCalculator() {
        return (priceCents, discountPercent) ->
                priceCents * (100 - discountPercent) / 100;
    }

    public static Runnable greetingTask(String name, Consumer<String> output) {
        return () -> output.accept("你好，" + name + "！");
    }
}
