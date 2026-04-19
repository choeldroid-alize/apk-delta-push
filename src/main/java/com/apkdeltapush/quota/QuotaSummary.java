package com.apkdeltapush.quota;

import java.time.Instant;

/**
 * Immutable snapshot of quota usage for a push session.
 */
public class QuotaSummary {

    private final long maxBytes;
    private final long usedBytes;
    private final Instant sessionStart;
    private final boolean exceeded;

    public QuotaSummary(long maxBytes, long usedBytes, Instant sessionStart, boolean exceeded) {
        this.maxBytes = maxBytes;
        this.usedBytes = usedBytes;
        this.sessionStart = sessionStart;
        this.exceeded = exceeded;
    }

    public long getMaxBytes() { return maxBytes; }
    public long getUsedBytes() { return usedBytes; }
    public long getRemainingBytes() { return Math.max(0, maxBytes - usedBytes); }
    public Instant getSessionStart() { return sessionStart; }
    public boolean isExceeded() { return exceeded; }

    public double getUsagePercent() {
        return maxBytes == 0 ? 0 : (usedBytes * 100.0) / maxBytes;
    }

    @Override
    public String toString() {
        return String.format("QuotaSummary{used=%d, max=%d, %.1f%%, exceeded=%b}",
            usedBytes, maxBytes, getUsagePercent(), exceeded);
    }
}
