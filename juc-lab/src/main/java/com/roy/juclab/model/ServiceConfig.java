package com.roy.juclab.model;

import java.util.Objects;

public final class ServiceConfig {
    private final long version;
    private final String endpoint;
    private final int timeoutMillis;

    public ServiceConfig(long version,
                         String endpoint,
                         int timeoutMillis) {
        if (version < 0) {
            throw new IllegalArgumentException("version 不能小于 0");
        }
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException(
                    "timeoutMillis 必须大于 0");
        }
        this.version = version;
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.timeoutMillis = timeoutMillis;
    }

    public long getVersion() {
        return version;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ServiceConfig)) {
            return false;
        }
        ServiceConfig that = (ServiceConfig) other;
        return version == that.version
                && timeoutMillis == that.timeoutMillis
                && endpoint.equals(that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, endpoint, timeoutMillis);
    }

    @Override
    public String toString() {
        return "ServiceConfig{"
                + "version=" + version
                + ", endpoint='" + endpoint + '\''
                + ", timeoutMillis=" + timeoutMillis
                + '}';
    }
}
