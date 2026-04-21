package com.apkdeltapush.stats;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects and aggregates push operation statistics across sessions.
 */
public class PushStatsCollector {

    private final AtomicInteger totalPushAttempts = new AtomicInteger(0);
    private final AtomicInteger successfulPushes = new AtomicInteger(0);
    private final AtomicInteger failedPushes = new AtomicInteger(0);
    private final AtomicLong totalBytesTransferred = new AtomicLong(0);
    private final AtomicLong totalDurationMs = new AtomicLong(0);
    private final Instant startedAt;

    public PushStatsCollector() {
        this.startedAt = Instant.now();
    }

    public void recordPushAttempt() {
        totalPushAttempts.incrementAndGet();
    }

    public void recordSuccess(long bytesTransferred, long durationMs) {
        if (bytesTransferred < 0) throw new IllegalArgumentException("bytesTransferred must be non-negative");
        if (durationMs < 0) throw new IllegalArgumentException("durationMs must be non-negative");
        successfulPushes.incrementAndGet();
        totalBytesTransferred.addAndGet(bytesTransferred);
        totalDurationMs.addAndGet(durationMs);
    }

    public void recordFailure() {
        failedPushes.incrementAndGet();
    }

    public PushStatsSummary buildSummary() {
        int attempts = totalPushAttempts.get();
        int successes = successfulPushes.get();
        int failures = failedPushes.get();
        long bytes = totalBytesTransferred.get();
        long duration = totalDurationMs.get();
        double successRate = attempts == 0 ? 0.0 : (double) successes / attempts * 100.0;
        double avgDurationMs = successes == 0 ? 0.0 : (double) duration / successes;
        return new PushStatsSummary(attempts, successes, failures, bytes, duration, successRate, avgDurationMs, startedAt);
    }

    public void reset() {
        totalPushAttempts.set(0);
        successfulPushes.set(0);
        failedPushes.set(0);
        totalBytesTransferred.set(0);
        totalDurationMs.set(0);
    }

    public int getTotalPushAttempts() { return totalPushAttempts.get(); }
    public int getSuccessfulPushes() { return successfulPushes.get(); }
    public int getFailedPushes() { return failedPushes.get(); }
    public long getTotalBytesTransferred() { return totalBytesTransferred.get(); }
}
