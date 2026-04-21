package com.apkdeltapush.stats;

import java.time.Instant;

/**
 * Immutable snapshot of aggregated push statistics.
 */
public class PushStatsSummary {

    private final int totalAttempts;
    private final int successfulPushes;
    private final int failedPushes;
    private final long totalBytesTransferred;
    private final long totalDurationMs;
    private final double successRatePercent;
    private final double avgDurationMs;
    private final Instant collectedAt;

    public PushStatsSummary(int totalAttempts, int successfulPushes, int failedPushes,
                            long totalBytesTransferred, long totalDurationMs,
                            double successRatePercent, double avgDurationMs, Instant collectedAt) {
        this.totalAttempts = totalAttempts;
        this.successfulPushes = successfulPushes;
        this.failedPushes = failedPushes;
        this.totalBytesTransferred = totalBytesTransferred;
        this.totalDurationMs = totalDurationMs;
        this.successRatePercent = successRatePercent;
        this.avgDurationMs = avgDurationMs;
        this.collectedAt = collectedAt;
    }

    public int getTotalAttempts() { return totalAttempts; }
    public int getSuccessfulPushes() { return successfulPushes; }
    public int getFailedPushes() { return failedPushes; }
    public long getTotalBytesTransferred() { return totalBytesTransferred; }
    public long getTotalDurationMs() { return totalDurationMs; }
    public double getSuccessRatePercent() { return successRatePercent; }
    public double getAvgDurationMs() { return avgDurationMs; }
    public Instant getCollectedAt() { return collectedAt; }

    @Override
    public String toString() {
        return String.format(
            "PushStatsSummary{attempts=%d, successes=%d, failures=%d, bytes=%d, successRate=%.1f%%, avgDuration=%.1fms, collectedAt=%s}",
            totalAttempts, successfulPushes, failedPushes, totalBytesTransferred,
            successRatePercent, avgDurationMs, collectedAt
        );
    }
}
