package com.roy.lambdalab.common;

import com.roy.lambdalab.model.Product;

import java.util.Arrays;
import java.util.List;

public final class SampleProducts {
    private SampleProducts() {
    }

    public static List<Product> create() {
        return Arrays.asList(
                new Product("机械键盘", 19_900, 12),
                new Product("无线鼠标", 9_900, 5),
                new Product("已售罄耳机", 15_900, 0),
                new Product("4K显示器", 129_900, 3)
        );
    }
}
