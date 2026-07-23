package com.roy.futurelab.exercises;

import com.roy.futurelab.common.DemoServices;

import java.util.concurrent.*;

/*
 * 第 2 课：给慢评价服务设置等待上限。
 *
 * 【背景知识：阻塞等待也要有边界】
 *
 * Future.get() 会一直等待任务结束。远程服务可能变慢、失联或长时间不返回，
 * 如果请求线程无限等待，最终可能耗尽应用中的线程。带超时的
 * get(timeout, unit) 给等待设置了时间预算；预算耗尽时抛出
 * TimeoutException，调用方可以及时返回降级结果。
 *
 * 【超时和取消不是同一件事】
 *
 * 超时只表示“当前线程不再等了”，后台任务并不会因此自动停止。
 * cancel(true) 会尝试取消任务：如果任务正在运行，它会向执行线程发送中断请求。
 * 这是协作式取消，不是强制终止。任务若忽略中断，仍可能继续运行。
 *
 * 【API 详解】
 *
 * 1. future.get(timeout, unit)
 *
 * - timeout：最多等待的数量，类型是 long。例如 200 表示“200 个时间单位”。
 * - unit：时间单位，例如 TimeUnit.MILLISECONDS。它决定 timeout 按毫秒、秒还是其他单位解释。
 * - 返回值：任务在期限内完成时，返回任务结果。
 * - 可能异常：
 *   TimeoutException 表示等待期限已到；
 *   ExecutionException 表示任务内部失败；
 *   InterruptedException 表示当前等待线程被中断。
 * - 注意：TimeoutException 不会自动取消 future。
 *
 * 2. future.cancel(mayInterruptIfRunning)
 *
 * - mayInterruptIfRunning：任务已经开始时，是否允许向执行线程发送中断请求。
 *   true 表示允许请求中断；false 表示不打断已经运行的任务。
 * - 返回值：boolean，表示本次取消请求是否成功改变了任务状态。
 * - 注意：返回 true 也不等于业务代码已经立刻停止，任务仍需主动响应中断。
 *
 * 3. executionException.getCause()
 *
 * - 参数：没有参数。
 * - 返回值：异步任务最初抛出的真实异常。
 * - 用途：ExecutionException 是 Future.get() 加上的外包装，错误信息通常应从 cause 中取得。
 *
 * 4. Thread.currentThread().interrupt()
 *
 * - currentThread()：取得正在执行当前代码的线程。
 * - interrupt()：重新设置该线程的中断标志。
 * - 用途：捕获 InterruptedException 后，如果当前层不负责消化中断，就恢复标志并继续上抛。
 *
 * 【三类异常代表三种不同问题】
 *
 * - TimeoutException：等待预算用完，不代表任务内部一定失败。
 * - ExecutionException：异步任务内部抛出了异常；真实异常保存在 getCause() 中。
 * - InterruptedException：当前等待线程被要求停止等待。捕获后通常要重新调用
 *   Thread.currentThread().interrupt() 保留中断信号，再向上抛出。
 *
 * 【为什么要保留中断标志】
 *
 * 抛出 InterruptedException 时，JVM 会清除线程的中断标志。
 * 如果当前方法无法完整处理这次中断，就应该恢复标志，让更上层代码仍能感知取消请求。
 * 吞掉中断可能导致应用无法及时关闭或任务无法取消。
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
