package com.roy.lambdalab.answers;

import com.roy.lambdalab.common.Money;
import com.roy.lambdalab.model.Product;

import java.util.function.Function;
import java.util.function.Predicate;

public final class LambdaLab02FunctionalInterfacesAnswer {
    private LambdaLab02FunctionalInterfacesAnswer() {
    }

    public static Predicate<Product> affordableAndInStock(int maxPriceCents) {
        return product -> product.getStock() > 0
                && product.getPriceCents() <= maxPriceCents;
    }

    public static Function<Product, String> displayLabel() {
        return product -> product.getName()
                + " | " + Money.format(product.getPriceCents())
                + " | 库存" + product.getStock();
    }
}
