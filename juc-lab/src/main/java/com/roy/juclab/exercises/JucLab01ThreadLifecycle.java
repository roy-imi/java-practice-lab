package com.roy.juclab.exercises;

/*
 * Lab 01：线程生命周期、等待完成与协作式中断。
 *
 * 课前文档：docs/guides/JUC_CONCURRENCY_GUIDE.md（Lab 01）
 * 练习重点：start 与 run 的区别、join、interrupt，以及线程命名。
 */
public final class JucLab01ThreadLifecycle {
    private JucLab01ThreadLifecycle() {
    }

    public static Thread startTask(String threadName, Runnable task) {
        /*
         * TODO：创建指定名称的 Thread，调用 start 后返回该线程。
         * 不要直接调用 task.run() 或 thread.run()。
         */
        throw new UnsupportedOperationException("TODO: 完成 JUC Lab 01 的线程启动");
    }

    public static void waitFor(Thread thread) throws InterruptedException {
        /*
         * TODO：等待目标线程执行结束。
         */
        throw new UnsupportedOperationException("TODO: 完成 JUC Lab 01 的线程等待");
    }

    public static void requestStop(Thread thread) {
        /*
         * TODO：发送中断请求。不要使用已经废弃且不安全的 Thread.stop()。
         */
        throw new UnsupportedOperationException("TODO: 完成 JUC Lab 01 的中断请求");
    }
}
