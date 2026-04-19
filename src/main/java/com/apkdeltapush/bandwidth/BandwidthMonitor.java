package com.apkdeltapush.bandwidth;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Monitors bandwidth usage during APK delta push operations.
 * Tracks bytes transferred and calculates transfer rates.
 */
public class BandwidthMonitor {

    private final AtomicLong totalBytesTransferred = new AtomicLong(0);
    private long startTimeMs = -1;
    private long endTimeMs = -1;

    public void start() {
        startTimeMs = System.currentTimeMillis();
        endTimeMs = -1;
        totalBytesTransferred.set(0);
    }

    public void recordTransfer(long bytes) {
        if (bytes < 0) throw new IllegalArgumentException("Bytes must be non-negative");
        totalBytesTransferred.addAndGet(bytes);
    }

    public void stop() {
        if (startTimeMs < 0) throw new IllegalStateException("Monitor was not started");
        endTimeMs = System.currentTimeMillis();
    }

    public long getTotalBytesTransferred() {
        return totalBytesTransferred.get();
    }

    /**
     * Returns average transfer rate in bytes per second.
     * Uses current time if monitor has not been stopped.
     */
    public double getAverageRateBytesPerSecond() {
        if (startTimeMs < 0) return 0.0;
        long end = endTimeMs >= 0 ? endTimeMs : System.currentTimeMillis();
        long elapsedMs = end - startTimeMs;
        if (elapsedMs == 0) return 0.0;
        return (totalBytesTransferred.get() * 1000.0) / elapsedMs;
    }

    public BandwidthSummary getSummary() {
        return new BandwidthSummary(
            totalBytesTransferred.get(),
            getAverageRateBytesPerSecond(),
            startTimeMs,
            endTimeMs >= 0 ? endTimeMs : System.currentTimeMillis()
        );
    }

    public boolean isRunning() {
        return startTimeMs >= 0 && endTimeMs < 0;
    }
}
