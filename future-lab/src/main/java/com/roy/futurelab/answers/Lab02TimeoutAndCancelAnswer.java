package com.roy.futurelab.answers;

import com.roy.futurelab.common.DemoServices;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Lab02TimeoutAndCancelAnswer {
    private Lab02TimeoutAndCancelAnswer() {
    }

    public static String loadReviewWithTimeout(DemoServices services,
                                               ExecutorService executor,
                                               String productName,
                                               long timeoutMillis)
            throws InterruptedException {
        Future<String> reviewFuture =
                executor.submit(() -> services.queryReviewSummary(productName));

        try {
            return reviewFuture.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException timeout) {
            reviewFuture.cancel(true);
            return "评价加载超时";
        } catch (ExecutionException failedTask) {
            Throwable cause = failedTask.getCause();
            return "评价服务异常：" + cause.getMessage();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw interrupted;
        }
    }
}
