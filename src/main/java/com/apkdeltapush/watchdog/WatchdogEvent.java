package com.apkdeltapush.watchdog;

import java.time.Instant;

/**
 * Carries information about a stall event detected by the {@link PushWatchdog}.
 */
public class WatchdogEvent {

    private final String sessionId;
    private final long stalledForMs;
    private final long thresholdMs;
    private final Instant detectedAt;

    public WatchdogEvent(String sessionId, long stalledForMs, long thresholdMs) {
        this.sessionId = sessionId;
        this.stalledForMs = stalledForMs;
        this.thresholdMs = thresholdMs;
        this.detectedAt = Instant.now();
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getStalledForMs() {
        return stalledForMs;
    }

    public long getThresholdMs() {
        return thresholdMs;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    @Override
    public String toString() {
        return "WatchdogEvent{sessionId='" + sessionId + "', stalledForMs=" + stalledForMs
                + ", thresholdMs=" + thresholdMs + ", detectedAt=" + detectedAt + "}";
    }
}
