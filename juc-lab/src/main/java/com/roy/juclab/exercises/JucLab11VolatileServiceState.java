package com.roy.juclab.exercises;

import com.roy.juclab.model.ServiceConfig;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * Lab 11：用 volatile 发布停止信号和不可变配置快照。
 *
 * 课前文档：docs/guides/JUC_CONCURRENCY_GUIDE.md（Lab 11）
 * 练习重点：可见性、安全发布，以及 volatile 不能保证复合操作原子性。
 */
public final class JucLab11VolatileServiceState {
    /*
     * TODO：为 running 和 config 增加 volatile 修饰符。
     * 验收器会检查字段修饰符，而不依赖偶发的线程时序。
     */
    private boolean running = true;
    private ServiceConfig config;

    /*
     * 请求计数是 read-modify-write 复合操作，不能只使用 volatile int，
     * 因此这里使用 AtomicInteger。
     */
    private final AtomicInteger processedRequests = new AtomicInteger();

    public JucLab11VolatileServiceState(ServiceConfig initialConfig) {
        if (initialConfig == null) {
            throw new NullPointerException("initialConfig");
        }
        this.config = initialConfig;
    }

    public boolean isRunning() {
        /*
         * TODO：读取运行标记。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 11 的运行状态读取");
    }

    public void requestStop() {
        /*
         * TODO：把运行标记设置为 false。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 11 的停止信号");
    }

    public ServiceConfig currentConfig() {
        /*
         * TODO：返回当前不可变配置快照。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 11 的配置读取");
    }

    public void updateConfig(ServiceConfig newConfig) {
        /*
         * TODO：拒绝 null，并用一次引用写入替换整个不可变快照。
         * 不要把 ServiceConfig 改成可变对象后逐字段更新。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 11 的配置发布");
    }

    public int recordProcessedRequest() {
        /*
         * TODO：使用 AtomicInteger 原子递增，并返回递增后的值。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 11 的原子计数");
    }

    public int getProcessedRequests() {
        /*
         * TODO：返回当前请求计数。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 11 的计数读取");
    }
}
