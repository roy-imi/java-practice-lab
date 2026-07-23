package com.roy.futurelab.common;

import com.roy.futurelab.model.ShopQuote;

import java.util.concurrent.CancellationException;

/**
 * 用 sleep 模拟远程调用。延迟和返回值保持固定，方便你观察并发效果。
 */
public final class DemoServices {

    public int queryPriceCents(String productName) {
        pause(360);
        if (productName.contains("价格故障")) {
            throw new IllegalStateException("价格服务暂时不可用");
        }
        return 19_900;
    }

    public int queryStock(String productName) {
        pause(520);
        return 12;
    }

    public String queryReviewSummary(String productName) {
        pause(1_200);
        if (productName.contains("评价故障")) {
            throw new IllegalStateException("评价服务返回 503");
        }
        return "96% 好评";
    }

    public String queryUserLevel(String userId) {
        pause(220);
        return "GOLD";
    }

    public int queryDiscountPercent(String userLevel) {
        pause(260);
        return "GOLD".equals(userLevel) ? 15 : 0;
    }

    public int queryDeliveryFeeCents(String city) {
        pause(300);
        return "上海".equals(city) ? 1_200 : 2_000;
    }

    public String fetchRecommendation(String productName) {
        pause(recommendationDelay(productName));
        if (productName.contains("已下架")) {
            throw new IllegalStateException(productName + " 无法推荐");
        }
        return productName + "：推荐指数 9.2";
    }

    public ShopQuote queryShopQuote(String shopName) {
        if ("A店".equals(shopName)) {
            pause(600);
            return new ShopQuote(shopName, 18_800);
        }
        if ("B店".equals(shopName)) {
            pause(250);
            return new ShopQuote(shopName, 19_200);
        }
        pause(450);
        return new ShopQuote(shopName, 18_600);
    }

    private long recommendationDelay(String productName) {
        if (productName.contains("鼠标")) {
            return 180;
        }
        if (productName.contains("已下架")) {
            return 260;
        }
        return 320;
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new CancellationException("任务收到中断请求");
        }
    }
}
