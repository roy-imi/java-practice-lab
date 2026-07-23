package com.roy.futurelab;

import com.roy.futurelab.answers.Lab01FutureBasicsAnswer;
import com.roy.futurelab.answers.Lab02TimeoutAndCancelAnswer;
import com.roy.futurelab.answers.Lab03PipelineAnswer;
import com.roy.futurelab.answers.Lab04ComposeAndCombineAnswer;
import com.roy.futurelab.answers.Lab05BatchAndRecoveryAnswer;
import com.roy.futurelab.answers.Lab06RaceAndRunAnswer;
import com.roy.futurelab.common.DemoExecutors;
import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.model.ShopQuote;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;

public final class AnswerConsole {
    private AnswerConsole() {
    }

    public static void main(String[] args) throws Exception {
        int lesson = args.length == 0 ? 1 : Integer.parseInt(args[0]);
        DemoServices services = new DemoServices();
        ExecutorService executor = DemoExecutors.newPool("answer-" + lesson, 6);
        long startedAt = System.currentTimeMillis();

        System.out.println("=== 第 " + lesson + " 课参考实现效果 ===");
        try {
            switch (lesson) {
                case 1:
                    System.out.println(Lab01FutureBasicsAnswer.loadProductSummary(
                            services, executor, "机械键盘", System.out::println));
                    break;
                case 2:
                    System.out.println(Lab02TimeoutAndCancelAnswer.loadReviewWithTimeout(
                            services, executor, "机械键盘", 150));
                    break;
                case 3:
                    Lab03PipelineAnswer.notifyWhenReady(
                            Lab03PipelineAnswer.buildVipPriceLabel(
                                    services, executor, "机械键盘"),
                            label -> System.out.println("通知用户：" + label)
                    ).join();
                    break;
                case 4:
                    System.out.println(Lab04ComposeAndCombineAnswer.buildCheckout(
                            services, executor, "机械键盘", "user-001", "上海").join());
                    break;
                case 5:
                    Lab05BatchAndRecoveryAnswer.loadRecommendations(
                            services,
                            executor,
                            Arrays.asList("无线鼠标", "已下架耳机", "显示器"),
                            event -> System.out.println("事件：" + event)
                    ).join().forEach(System.out::println);
                    break;
                case 6:
                    ShopQuote quote = Lab06RaceAndRunAnswer.firstCompletedQuote(
                            services,
                            executor,
                            Arrays.asList("A店", "B店", "C店")
                    ).join();
                    System.out.println("最快结果：" + quote);
                    Lab06RaceAndRunAnswer.writeAuditLogAsync(
                            quote, executor, System.out::println).join();
                    break;
                default:
                    throw new IllegalArgumentException("课次只能是 1 到 6");
            }
            System.out.println("完成，用时 "
                    + (System.currentTimeMillis() - startedAt) + "ms");
        } finally {
            DemoExecutors.shutdown(executor);
        }
    }
}
