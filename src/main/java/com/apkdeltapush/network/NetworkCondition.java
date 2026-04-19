package com.apkdeltapush.network;

import java.time.Instant;

/**
 * Immutable snapshot of network conditions at a point in time.
 */
public class NetworkCondition {

    private final long latencyMs;
    private final long throughputBytesPerSec;
    private final Instant measuredAt;

    public NetworkCondition(long latencyMs, long throughputBytesPerSec, Instant measuredAt) {
        if (latencyMs < 0) throw new IllegalArgumentException("latencyMs must be >= 0");
        if (throughputBytesPerSec < 0) throw new IllegalArgumentException("throughput must be >= 0");
        if (measuredAt == null) throw new IllegalArgumentException("measuredAt must not be null");
        this.latencyMs = latencyMs;
        this.throughputBytesPerSec = throughputBytesPerSec;
        this.measuredAt = measuredAt;
    }

    public long getLatencyMs() { return latencyMs; }
    public long getThroughputBytesPerSec() { return throughputBytesPerSec; }
    public Instant getMeasuredAt() { return measuredAt; }

    /** Estimated seconds to transfer the given number of bytes, or -1 if throughput is unknown. */
    public double estimateTransferSeconds(long bytes) {
        if (throughputBytesPerSec == 0) return -1;
        return (double) bytes / throughputBytesPerSec;
    }

    @Override
    public String toString() {
        return String.format("NetworkCondition{latency=%dms, throughput=%d B/s, at=%s}",
                latencyMs, throughputBytesPerSec, measuredAt);
    }
}
