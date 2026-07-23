package com.roy.futurelab;

import com.roy.futurelab.common.DemoExecutors;
import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.exercises.Lab01FutureBasics;
import com.roy.futurelab.exercises.Lab02TimeoutAndCancel;
import com.roy.futurelab.exercises.Lab03Pipeline;
import com.roy.futurelab.exercises.Lab04ComposeAndCombine;
import com.roy.futurelab.exercises.Lab05BatchAndRecovery;
import com.roy.futurelab.exercises.Lab06RaceAndRun;
import com.roy.futurelab.model.ShopQuote;

import java.util.Arrays;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

public final class LearningConsole {
    private LearningConsole() {
    }

    public static void main(String[] args) {
        int lesson = parseLesson(args);
        DemoServices services = new DemoServices();
        ExecutorService executor = DemoExecutors.newPool("lab-" + lesson, 6);

        System.out.println("=== 运行第 " + lesson + " 课练习 ===");
        long startedAt = System.currentTimeMillis();
        try {
            runLesson(lesson, services, executor);
            System.out.println("完成，用时 "
                    + (System.currentTimeMillis() - startedAt) + "ms");
        } catch (UnsupportedOperationException todo) {
            System.out.println(todo.getMessage());
            System.out.println("请修改 exercises/Lab0" + lesson
                    + "...，需要时查看 docs/hints/lab-0" + lesson + ".md");
        } catch (CompletionException asyncFailure) {
            Throwable cause = asyncFailure.getCause();
            System.out.println("异步任务失败：" + cause);
        } catch (Exception failure) {
            System.out.println("执行失败：" + failure);
        } finally {
            DemoExecutors.shutdown(executor);
        }
    }

    private static void runLesson(int lesson,
                                  DemoServices services,
                                  ExecutorService executor)
            throws Exception {
        switch (lesson) {
            case 1:
                System.out.println(Lab01FutureBasics.loadProductSummary(
                        services, executor, "机械键盘", System.out::println));
                break;
            case 2:
                System.out.println(Lab02TimeoutAndCancel.loadReviewWithTimeout(
                        services, executor, "机械键盘", 150));
                break;
            case 3:
                Lab03Pipeline.notifyWhenReady(
                        Lab03Pipeline.buildVipPriceLabel(
                                services, executor, "机械键盘"),
                        label -> System.out.println("通知用户：" + label)
                ).join();
                break;
            case 4:
                System.out.println(Lab04ComposeAndCombine.buildCheckout(
                        services, executor, "机械键盘", "user-001", "上海").join());
                break;
            case 5:
                Lab05BatchAndRecovery.loadRecommendations(
                        services,
                        executor,
                        Arrays.asList("无线鼠标", "已下架耳机", "显示器"),
                        event -> System.out.println("事件：" + event)
                ).join().forEach(System.out::println);
                break;
            case 6:
                ShopQuote quote = Lab06RaceAndRun.firstCompletedQuote(
                        services,
                        executor,
                        Arrays.asList("A店", "B店", "C店")
                ).join();
                System.out.println("最快结果：" + quote);
                Lab06RaceAndRun.writeAuditLogAsync(
                        quote, executor, System.out::println).join();
                break;
            default:
                throw new IllegalArgumentException("课次只能是 1 到 6");
        }
    }

    private static int parseLesson(String[] args) {
        if (args.length == 0) {
            System.out.println("用法：LearningConsole <1-6>，默认运行第 1 课");
            return 1;
        }
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException invalid) {
            throw new IllegalArgumentException("课次必须是数字 1 到 6", invalid);
        }
    }
}
