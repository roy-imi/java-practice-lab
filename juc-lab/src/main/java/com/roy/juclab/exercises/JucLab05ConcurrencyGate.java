package com.roy.juclab.exercises;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/*
 * Lab 05：控制“何时开始”和“最多同时执行多少个任务”。
 *
 * 课前文档：docs/guides/JUC_CONCURRENCY_GUIDE.md（Lab 05）
 * 练习重点：CountDownLatch.await、Semaphore.acquire/release 和中断处理。
 */
public final class JucLab05ConcurrencyGate {
    private JucLab05ConcurrencyGate() {
    }

    public static Runnable waitForStart(CountDownLatch startSignal,
                                        Runnable task) {
        /*
         * TODO：返回一个 Runnable：
         * - 先等待 startSignal；
         * - 再执行 task；
         * - 等待时被中断则恢复中断标记，并且不执行 task。
         */
        throw new UnsupportedOperationException("TODO: 完成 JUC Lab 05 的启动协调");
    }

    public static Runnable limitConcurrency(Semaphore permits,
                                            Runnable task) {
        /*
         * TODO：返回一个 Runnable：
         * - 执行任务前 acquire；
         * - task 无论正常结束还是抛异常都必须 release；
         * - acquire 时被中断则恢复中断标记，并且不能错误 release。
         */
        throw new UnsupportedOperationException("TODO: 完成 JUC Lab 05 的并发限流");
    }
}
