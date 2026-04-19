package com.apkdeltapush.quota;

import java.util.concurrent.atomic.AtomicLong;
import java.time.Instant;

/**
 * Manages transfer quota limits per device session to prevent
 * excessive data usage during delta push operations.
 */
public class TransferQuotaManager {

    private final long maxBytesPerSession;
    private final AtomicLong bytesTransferred = new AtomicLong(0);
    private final Instant sessionStart;
    private volatile boolean quotaExceeded = false;

    public TransferQuotaManager(long maxBytesPerSession) {
        if (maxBytesPerSession <= 0) {
            throw new IllegalArgumentException("maxBytesPerSession must be positive");
        }
        this.maxBytesPerSession = maxBytesPerSession;
        this.sessionStart = Instant.now();
    }

    /**
     * Records bytes transferred and checks quota.
     * @param bytes number of bytes to record
     * @throws QuotaExceededException if quota is exceeded after recording
     */
    public void recordTransfer(long bytes) throws QuotaExceededException {
        if (bytes < 0) throw new IllegalArgumentException("bytes must be non-negative");
        long total = bytesTransferred.addAndGet(bytes);
        if (total > maxBytesPerSession) {
            quotaExceeded = true;
            throw new QuotaExceededException(
                String.format("Transfer quota exceeded: %d / %d bytes", total, maxBytesPerSession));
        }
    }

    public long getBytesTransferred() {
        return bytesTransferred.get();
    }

    public long getRemainingBytes() {
        return Math.max(0, maxBytesPerSession - bytesTransferred.get());
    }

    public boolean isQuotaExceeded() {
        return quotaExceeded;
    }

    public double getUsagePercent() {
        return (bytesTransferred.get() * 100.0) / maxBytesPerSession;
    }

    public QuotaSummary getSummary() {
        return new QuotaSummary(maxBytesPerSession, bytesTransferred.get(), sessionStart, quotaExceeded);
    }

    public void reset() {
        bytesTransferred.set(0);
        quotaExceeded = false;
    }
}
