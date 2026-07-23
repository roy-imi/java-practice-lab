package com.roy.lambdalab.answers;

import com.roy.lambdalab.model.Product;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class LambdaLab04StreamsAnswer {
    private LambdaLab04StreamsAnswer() {
    }

    public static List<String> findAffordableProductNames(List<Product> products,
                                                          int maxPriceCents) {
        return products.stream()
                .filter(product -> product.getStock() > 0
                        && product.getPriceCents() <= maxPriceCents)
                .sorted(Comparator.comparingInt(Product::getPriceCents))
                .map(Product::getName)
                .collect(Collectors.toList());
    }
}
