package com.roy.juclab.answers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Function;

public final class JucLab06BlockingQueuePipelineAnswer {
    private JucLab06BlockingQueuePipelineAnswer() {
    }

    public static List<String> process(
            List<String> inputs,
            int workerCount,
            Function<String, String> processor)
            throws InterruptedException {
        if (workerCount <= 0) {
            throw new IllegalArgumentException("workerCount 必须大于 0");
        }

        int queueCapacity = Math.max(1, workerCount * 2);
        BlockingQueue<WorkItem> queue =
                new ArrayBlockingQueue<>(queueCapacity);
        AtomicReferenceArray<String> results =
                new AtomicReferenceArray<>(inputs.size());
        WorkItem poison = new WorkItem(-1, null);
        List<Thread> workers = new ArrayList<>();

        for (int workerIndex = 0;
             workerIndex < workerCount;
             workerIndex++) {
            Thread worker = new Thread(() -> {
                try {
                    while (true) {
                        WorkItem item = queue.take();
                        if (item == poison) {
                            return;
                        }
                        results.set(
                                item.index,
                                processor.apply(item.value));
                    }
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            }, "queue-worker-" + (workerIndex + 1));
            worker.start();
            workers.add(worker);
        }

        for (int index = 0; index < inputs.size(); index++) {
            queue.put(new WorkItem(index, inputs.get(index)));
        }
        for (int index = 0; index < workerCount; index++) {
            queue.put(poison);
        }
        for (Thread worker : workers) {
            worker.join();
        }

        List<String> orderedResults = new ArrayList<>();
        for (int index = 0; index < inputs.size(); index++) {
            orderedResults.add(results.get(index));
        }
        return orderedResults;
    }

    private static final class WorkItem {
        private final int index;
        private final String value;

        private WorkItem(int index, String value) {
            this.index = index;
            this.value = value;
        }
    }
}
