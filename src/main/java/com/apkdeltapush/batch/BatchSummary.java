package com.apkdeltapush.batch;

/**
 * Aggregate summary of a completed batch push operation.
 */
public class BatchSummary {

    private final int total;
    private final int successCount;
    private final int failureCount;

    public BatchSummary(int total, int successCount, int failureCount) {
        this.total = total;
        this.successCount = successCount;
        this.failureCount = failureCount;
    }

    public int getTotal() { return total; }
    public int getSuccessCount() { return successCount; }
    public int getFailureCount() { return failureCount; }

    public double getSuccessRate() {
        if (total == 0) return 0.0;
        return (double) successCount / total * 100.0;
    }

    public boolean isFullySuccessful() {
        return total > 0 && failureCount == 0;
    }

    @Override
    public String toString() {
        return String.format("BatchSummary{total=%d, success=%d, failure=%d, rate=%.1f%%}",
                total, successCount, failureCount, getSuccessRate());
    }
}
