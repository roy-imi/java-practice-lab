package com.roy.juclab.exercises;

import java.util.List;
import java.util.function.Function;

/*
 * Lab 06：使用有界 BlockingQueue 建立生产者消费者流水线。
 *
 * 课前文档：docs/guides/JUC_CONCURRENCY_GUIDE.md（Lab 06）
 * 练习重点：put、take、背压、毒丸，以及等待消费者正常结束。
 */
public final class JucLab06BlockingQueuePipeline {
    private JucLab06BlockingQueuePipeline() {
    }

    public static List<String> process(
            List<String> inputs,
            int workerCount,
            Function<String, String> processor)
            throws InterruptedException {
        /*
         * TODO：
         * - workerCount 必须大于 0；
         * - 建立有界 BlockingQueue；
         * - 启动 workerCount 个消费者；
         * - 每个消费者持续 take，遇到毒丸后退出；
         * - 输入要携带原下标，保证返回结果与输入顺序一致；
         * - 所有普通任务之后，为每个消费者放入一个毒丸；
         * - join 所有消费者，再返回结果。
         */
        throw new UnsupportedOperationException("TODO: 完成 JUC Lab 06 的阻塞队列");
    }
}
