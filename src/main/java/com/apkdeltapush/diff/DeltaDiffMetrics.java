package com.apkdeltapush.diff;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks runtime metrics for delta diff operations, including counts,
 * timings, and byte-level statistics.
 */
public class DeltaDiffMetrics {

    private final AtomicLong totalDiffsComputed = new AtomicLong(0);
    private final AtomicLong totalDiffsFailed = new AtomicLong(0);
    private final AtomicLong totalBytesIn = new AtomicLong(0);
    private final AtomicLong totalBytesOut = new AtomicLong(0);
    private final AtomicLong totalComputeTimeMs = new AtomicLong(0);
    private volatile Instant lastDiffTimestamp;

    public void recordDiff(long bytesIn, long bytesOut, long computeTimeMs) {
        if (bytesIn < 0 || bytesOut < 0 || computeTimeMs < 0) {
            throw new IllegalArgumentException("Metric values must be non-negative");
        }
        totalDiffsComputed.incrementAndGet();
        totalBytesIn.addAndGet(bytesIn);
        totalBytesOut.addAndGet(bytesOut);
        totalComputeTimeMs.addAndGet(computeTimeMs);
        lastDiffTimestamp = Instant.now();
    }

    public void recordFailure() {
        totalDiffsFailed.incrementAndGet();
    }

    public long getTotalDiffsComputed() {
        return totalDiffsComputed.get();
    }

    public long getTotalDiffsFailed() {
        return totalDiffsFailed.get();
    }

    public long getTotalBytesIn() {
        return totalBytesIn.get();
    }

    public long getTotalBytesOut() {
        return totalBytesOut.get();
    }

    public long getTotalComputeTimeMs() {
        return totalComputeTimeMs.get();
    }

    public double getAverageComputeTimeMs() {
        long count = totalDiffsComputed.get();
        return count == 0 ? 0.0 : (double) totalComputeTimeMs.get() / count;
    }

    public double getCompressionRatio() {
        long in = totalBytesIn.get();
        return in == 0 ? 0.0 : (double) totalBytesOut.get() / in;
    }

    public Instant getLastDiffTimestamp() {
        return lastDiffTimestamp;
    }

    public void reset() {
        totalDiffsComputed.set(0);
        totalDiffsFailed.set(0);
        totalBytesIn.set(0);
        totalBytesOut.set(0);
        totalComputeTimeMs.set(0);
        lastDiffTimestamp = null;
    }

    @Override
    public String toString() {
        return String.format(
            "DeltaDiffMetrics{computed=%d, failed=%d, bytesIn=%d, bytesOut=%d, avgMs=%.2f, ratio=%.4f}",
            getTotalDiffsComputed(), getTotalDiffsFailed(),
            getTotalBytesIn(), getTotalBytesOut(),
            getAverageComputeTimeMs(), getCompressionRatio()
        );
    }
}
