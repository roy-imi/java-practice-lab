package com.roy.futurelab.exercises;

import com.roy.futurelab.common.DemoServices;

import java.util.concurrent.*;

/*
 * 第 2 课：给慢评价服务设置等待上限。
 *
 * 课前文档：docs/guides/FUTURE_API_GUIDE.md（第 2 课）
 * 练习重点：带超时的 get、cancel、ExecutionException 和中断处理。
 *
 * 期望行为：
 * - 按时完成：返回真实评价；
 * - 超时：请求取消任务，返回“评价加载超时”；
 * - 任务异常：返回“评价服务异常：真正原因”；
 * - 当前线程被中断：恢复中断标志，并继续向上抛出。
 */
public final class Lab02TimeoutAndCancel {
    private Lab02TimeoutAndCancel() {
    }

    public static String loadReviewWithTimeout(DemoServices services,
                                               ExecutorService executor,
                                               String productName,
                                               long timeoutMillis)
            throws InterruptedException {
        /*
         * TODO 你的任务：
         * - submit queryReviewSummary；
         * - 不使用无期限 get()，而是最多等待 timeoutMillis；
         * - 分别处理 TimeoutException、ExecutionException、InterruptedException；
         * - 超时时调用 cancel(true)。
         */

        Future<String> reviewFuture = executor.submit(() -> services.queryReviewSummary(productName));

        try {
            return reviewFuture.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException timeout) {
            reviewFuture.cancel(true);
            return "评价服务超时";
        } catch (ExecutionException execution) {
            Throwable cause = execution.getCause();
            return "评价服务异常" + cause.getMessage();
        } catch (InterruptedException interrupt) {
            Thread.currentThread().interrupt();
            throw interrupt;
        }
    }
}
