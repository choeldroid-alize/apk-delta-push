package com.apkdeltapush.metrics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe collector that accumulates push metrics during a session
 * and produces {@link PushMetricsSnapshot} on demand.
 */
public class PushMetricsCollector {

    private final String sessionId;
    private final AtomicLong totalBytesSent = new AtomicLong(0);
    private final AtomicLong totalBytesReceived = new AtomicLong(0);
    private final AtomicInteger successfulPushes = new AtomicInteger(0);
    private final AtomicInteger failedPushes = new AtomicInteger(0);
    private final Map<String, AtomicLong> perDeviceBytesSent = new HashMap<>();
    private final Object deviceLock = new Object();

    private volatile long sessionStartMillis = System.currentTimeMillis();

    public PushMetricsCollector(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId must not be blank");
        }
        this.sessionId = sessionId;
    }

    public void recordBytesSent(String deviceId, long bytes) {
        if (bytes < 0) throw new IllegalArgumentException("bytes must be non-negative");
        totalBytesSent.addAndGet(bytes);
        synchronized (deviceLock) {
            perDeviceBytesSent.computeIfAbsent(deviceId, k -> new AtomicLong(0)).addAndGet(bytes);
        }
    }

    public void recordBytesReceived(long bytes) {
        if (bytes < 0) throw new IllegalArgumentException("bytes must be non-negative");
        totalBytesReceived.addAndGet(bytes);
    }

    public void recordSuccess() {
        successfulPushes.incrementAndGet();
    }

    public void recordFailure() {
        failedPushes.incrementAndGet();
    }

    public void reset() {
        totalBytesSent.set(0);
        totalBytesReceived.set(0);
        successfulPushes.set(0);
        failedPushes.set(0);
        synchronized (deviceLock) {
            perDeviceBytesSent.clear();
        }
        sessionStartMillis = System.currentTimeMillis();
    }

    public PushMetricsSnapshot snapshot() {
        long elapsedMs = Math.max(1, System.currentTimeMillis() - sessionStartMillis);
        double rateBps = (totalBytesSent.get() * 1000.0) / elapsedMs;

        Map<String, Long> deviceSnapshot = new HashMap<>();
        synchronized (deviceLock) {
            perDeviceBytesSent.forEach((k, v) -> deviceSnapshot.put(k, v.get()));
        }

        return new PushMetricsSnapshot(
                sessionId,
                Instant.now(),
                totalBytesSent.get(),
                totalBytesReceived.get(),
                successfulPushes.get(),
                failedPushes.get(),
                rateBps,
                deviceSnapshot
        );
    }

    public String getSessionId() { return sessionId; }
}
