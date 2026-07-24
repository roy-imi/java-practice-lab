package com.roy.juclab.exercises;

import com.roy.juclab.model.RequestContext;

import java.util.Optional;

/*
 * Lab 09：使用 ThreadLocal 管理请求上下文。
 *
 * 课前文档：docs/guides/JUC_CONCURRENCY_GUIDE.md（Lab 09）
 * 练习重点：线程隔离、get/set/remove、捕获上下文，以及执行后的恢复。
 */
public final class JucLab09ThreadLocalContext {
    private static final ThreadLocal<RequestContext> CURRENT =
            new ThreadLocal<>();

    private JucLab09ThreadLocalContext() {
    }

    public static void set(RequestContext context) {
        /*
         * TODO：保存当前线程的请求上下文。
         * context 为 null 时应该尽早抛出 NullPointerException。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 09 的上下文设置");
    }

    public static Optional<RequestContext> current() {
        /*
         * TODO：读取当前线程的上下文，并用 Optional 包装。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 09 的上下文读取");
    }

    public static void clear() {
        /*
         * TODO：使用 remove 清理当前线程的条目。
         * 不要只调用 set(null)。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 09 的上下文清理");
    }

    public static Runnable wrap(Runnable task) {
        /*
         * TODO：
         * - 在调用 wrap 的线程中捕获 CURRENT.get()；
         * - 返回新的 Runnable；
         * - 新 Runnable 执行前保存工作线程原有上下文；
         * - 安装捕获的上下文后执行 task；
         * - 在 finally 中恢复工作线程原有上下文；
         * - 原有上下文为 null 时使用 remove，而不是 set(null)。
         *
         * 这样既能向线程池任务传递上下文，也支持嵌套调用和异常恢复。
         */
        throw new UnsupportedOperationException(
                "TODO: 完成 JUC Lab 09 的上下文传播");
    }
}
