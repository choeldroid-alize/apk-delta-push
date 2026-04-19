package com.apkdeltapush.transfer;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Throttles data transfer to a configured maximum bytes-per-second rate.
 */
public class TransferThrottler {

    private final long maxBytesPerSecond;
    private final AtomicLong bytesTransferredInWindow = new AtomicLong(0);
    private volatile long windowStartMs;

    public TransferThrottler(long maxBytesPerSecond) {
        if (maxBytesPerSecond <= 0) {
            throw new IllegalArgumentException("maxBytesPerSecond must be positive");
        }
        this.maxBytesPerSecond = maxBytesPerSecond;
        this.windowStartMs = System.currentTimeMillis();
    }

    /**
     * Called before transferring a chunk. Blocks if the current rate exceeds the limit.
     *
     * @param chunkSizeBytes number of bytes about to be transferred
     * @throws InterruptedException if the thread is interrupted while sleeping
     */
    public synchronized void throttle(long chunkSizeBytes) throws InterruptedException {
        long now = System.currentTimeMillis();
        long elapsed = now - windowStartMs;

        if (elapsed >= 1000) {
            bytesTransferredInWindow.set(0);
            windowStartMs = now;
        }

        long projected = bytesTransferredInWindow.get() + chunkSizeBytes;
        if (projected > maxBytesPerSecond) {
            long sleepMs = 1000 - elapsed;
            if (sleepMs > 0) {
                Thread.sleep(sleepMs);
            }
            bytesTransferredInWindow.set(0);
            windowStartMs = System.currentTimeMillis();
        }

        bytesTransferredInWindow.addAndGet(chunkSizeBytes);
    }

    public long getMaxBytesPerSecond() {
        return maxBytesPerSecond;
    }

    public long getBytesTransferredInWindow() {
        return bytesTransferredInWindow.get();
    }

    /** Resets the current window counters. */
    public synchronized void reset() {
        bytesTransferredInWindow.set(0);
        windowStartMs = System.currentTimeMillis();
    }
}
