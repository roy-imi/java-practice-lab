package com.roy.lambdalab.answers;

import com.roy.lambdalab.model.Product;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class LambdaLab03MethodReferencesAnswer {
    private LambdaLab03MethodReferencesAnswer() {
    }

    public static Comparator<String> caseInsensitiveComparator() {
        return String::compareToIgnoreCase;
    }

    public static Function<Product, String> nameExtractor() {
        return Product::getName;
    }

    public static Consumer<String> listAppender(List<String> target) {
        return target::add;
    }

    public static Supplier<List<String>> listFactory() {
        return ArrayList::new;
    }
}
