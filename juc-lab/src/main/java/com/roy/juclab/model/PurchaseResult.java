package com.roy.juclab.model;

import java.util.Objects;

public final class PurchaseResult {
    private final String requestId;
    private final PurchaseStatus status;

    private PurchaseResult(String requestId, PurchaseStatus status) {
        this.requestId = Objects.requireNonNull(requestId, "requestId");
        this.status = Objects.requireNonNull(status, "status");
    }

    public static PurchaseResult success(String requestId) {
        return new PurchaseResult(requestId, PurchaseStatus.SUCCESS);
    }

    public static PurchaseResult soldOut(String requestId) {
        return new PurchaseResult(requestId, PurchaseStatus.SOLD_OUT);
    }

    public String getRequestId() {
        return requestId;
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == PurchaseStatus.SUCCESS;
    }

    @Override
    public String toString() {
        return "PurchaseResult{"
                + "requestId='" + requestId + '\''
                + ", status=" + status
                + '}';
    }
}
