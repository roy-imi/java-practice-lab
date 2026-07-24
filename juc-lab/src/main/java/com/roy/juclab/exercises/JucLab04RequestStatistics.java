package com.roy.juclab.exercises;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

/*
 * Lab 04：统计每个接口被并发调用的次数。
 *
 * 课前文档：docs/guides/JUC_CONCURRENCY_GUIDE.md（Lab 04）
 * 练习重点：ConcurrentHashMap、computeIfAbsent 和 LongAdder。
 */
public final class JucLab04RequestStatistics {
    private final ConcurrentMap<String, LongAdder> counts = new ConcurrentHashMap<>();

    public void record(String endpoint) {
        /*
         * TODO：原子地取得或创建 LongAdder，然后递增。
         * 不要使用 containsKey 后再 put 的“先检查再执行”组合。
         */
        counts.computeIfAbsent(
                    endpoint,
                    key -> new LongAdder()
                ).increment();
    }

    public long count(String endpoint) {
        /*
         * TODO：不存在时返回 0，否则返回 LongAdder 的当前值。
         */
        LongAdder longAdder = counts.get(endpoint);
        return longAdder == null ? 0L : longAdder.sum();
    }

    public Map<String, Long> snapshot() {
        /*
         * TODO：把当前统计复制到新的普通 Map 中。
         * 这是弱一致快照，不要求阻塞所有正在写入的线程。
         */
        Map<String, Long> result = new HashMap<>();
        counts.forEach(
                (endpoint, counter) -> result.put(endpoint, counter.sum())
        );
        return result;
    }
}
