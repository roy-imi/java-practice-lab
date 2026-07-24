package com.roy.juclab.model;

import java.util.Objects;

public final class RequestContext {
    private final String requestId;
    private final String userId;

    public RequestContext(String requestId, String userId) {
        this.requestId = Objects.requireNonNull(requestId, "requestId");
        this.userId = Objects.requireNonNull(userId, "userId");
    }

    public String getRequestId() {
        return requestId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RequestContext)) {
            return false;
        }
        RequestContext that = (RequestContext) other;
        return requestId.equals(that.requestId)
                && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, userId);
    }

    @Override
    public String toString() {
        return "RequestContext{"
                + "requestId='" + requestId + '\''
                + ", userId='" + userId + '\''
                + '}';
    }
}
