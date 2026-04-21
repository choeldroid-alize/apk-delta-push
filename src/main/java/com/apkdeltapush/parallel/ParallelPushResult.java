package com.apkdeltapush.parallel;

import java.time.Instant;

/**
 * Holds the outcome of a single device push executed within a parallel push session.
 */
public class ParallelPushResult {

    private final String deviceId;
    private final boolean success;
    private final String errorMessage;
    private final long durationMs;
    private final Instant completedAt;

    private ParallelPushResult(String deviceId, boolean success, String errorMessage, long durationMs) {
        this.deviceId = deviceId;
        this.success = success;
        this.errorMessage = errorMessage;
        this.durationMs = durationMs;
        this.completedAt = Instant.now();
    }

    public static ParallelPushResult success(String deviceId, long durationMs) {
        return new ParallelPushResult(deviceId, true, null, durationMs);
    }

    public static ParallelPushResult failure(String deviceId, String errorMessage) {
        return new ParallelPushResult(deviceId, false, errorMessage, 0L);
    }

    public static ParallelPushResult failure(String deviceId, String errorMessage, long durationMs) {
        return new ParallelPushResult(deviceId, false, errorMessage, durationMs);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    @Override
    public String toString() {
        return "ParallelPushResult{deviceId='" + deviceId +
                "', success=" + success +
                ", durationMs=" + durationMs +
                (success ? "" : ", error='" + errorMessage + "'") +
                "}";
    }
}
