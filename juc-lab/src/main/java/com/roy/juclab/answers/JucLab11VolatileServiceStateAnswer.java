package com.roy.juclab.answers;

import com.roy.juclab.model.ServiceConfig;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class JucLab11VolatileServiceStateAnswer {
    private volatile boolean running = true;
    private volatile ServiceConfig config;
    private final AtomicInteger processedRequests = new AtomicInteger();

    public JucLab11VolatileServiceStateAnswer(
            ServiceConfig initialConfig) {
        this.config =
                Objects.requireNonNull(initialConfig, "initialConfig");
    }

    public boolean isRunning() {
        return running;
    }

    public void requestStop() {
        running = false;
    }

    public ServiceConfig currentConfig() {
        return config;
    }

    public void updateConfig(ServiceConfig newConfig) {
        config = Objects.requireNonNull(newConfig, "newConfig");
    }

    public int recordProcessedRequest() {
        return processedRequests.incrementAndGet();
    }

    public int getProcessedRequests() {
        return processedRequests.get();
    }
}
