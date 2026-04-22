package com.apkdeltapush.metrics;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable snapshot of push metrics captured at a point in time.
 */
public class PushMetricsSnapshot {

    private final String sessionId;
    private final Instant capturedAt;
    private final long totalBytesSent;
    private final long totalBytesReceived;
    private final int successfulPushes;
    private final int failedPushes;
    private final double averageTransferRateBps;
    private final Map<String, Long> perDeviceBytesSent;

    public PushMetricsSnapshot(String sessionId, Instant capturedAt,
                                long totalBytesSent, long totalBytesReceived,
                                int successfulPushes, int failedPushes,
                                double averageTransferRateBps,
                                Map<String, Long> perDeviceBytesSent) {
        this.sessionId = sessionId;
        this.capturedAt = capturedAt;
        this.totalBytesSent = totalBytesSent;
        this.totalBytesReceived = totalBytesReceived;
        this.successfulPushes = successfulPushes;
        this.failedPushes = failedPushes;
        this.averageTransferRateBps = averageTransferRateBps;
        this.perDeviceBytesSent = Collections.unmodifiableMap(new HashMap<>(perDeviceBytesSent));
    }

    public String getSessionId() { return sessionId; }
    public Instant getCapturedAt() { return capturedAt; }
    public long getTotalBytesSent() { return totalBytesSent; }
    public long getTotalBytesReceived() { return totalBytesReceived; }
    public int getSuccessfulPushes() { return successfulPushes; }
    public int getFailedPushes() { return failedPushes; }
    public double getAverageTransferRateBps() { return averageTransferRateBps; }
    public Map<String, Long> getPerDeviceBytesSent() { return perDeviceBytesSent; }

    public int getTotalPushes() {
        return successfulPushes + failedPushes;
    }

    public double getSuccessRate() {
        int total = getTotalPushes();
        return total == 0 ? 0.0 : (double) successfulPushes / total * 100.0;
    }

    @Override
    public String toString() {
        return String.format("PushMetricsSnapshot{session=%s, sent=%d, success=%d, failed=%d, rate=%.2f%%}",
                sessionId, totalBytesSent, successfulPushes, failedPushes, getSuccessRate());
    }
}
