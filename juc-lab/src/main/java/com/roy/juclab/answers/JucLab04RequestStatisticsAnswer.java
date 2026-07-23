package com.roy.juclab.answers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

public final class JucLab04RequestStatisticsAnswer {
    private final ConcurrentMap<String, LongAdder> counts =
            new ConcurrentHashMap<>();

    public void record(String endpoint) {
        counts.computeIfAbsent(endpoint, key -> new LongAdder())
                .increment();
    }

    public long count(String endpoint) {
        LongAdder counter = counts.get(endpoint);
        return counter == null ? 0L : counter.sum();
    }

    public Map<String, Long> snapshot() {
        Map<String, Long> result = new HashMap<>();
        counts.forEach(
                (endpoint, counter) ->
                        result.put(endpoint, counter.sum()));
        return result;
    }
}
