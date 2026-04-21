package com.apkdeltapush.ratelimit;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Token-bucket rate limiter that controls how many push operations
 * can be initiated per time window (e.g. per second).
 */
public class RateLimiter {

    private final int maxTokens;
    private final long windowMillis;
    private final Deque<Long> tokenTimestamps = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();

    public RateLimiter(int maxTokens, long windowMillis) {
        if (maxTokens <= 0) throw new IllegalArgumentException("maxTokens must be > 0");
        if (windowMillis <= 0) throw new IllegalArgumentException("windowMillis must be > 0");
        this.maxTokens = maxTokens;
        this.windowMillis = windowMillis;
    }

    /**
     * Attempts to acquire a token. Returns true if allowed, false if rate limit exceeded.
     */
    public boolean tryAcquire() {
        lock.lock();
        try {
            long now = Instant.now().toEpochMilli();
            evictExpired(now);
            if (tokenTimestamps.size() < maxTokens) {
                tokenTimestamps.addLast(now);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the number of tokens available in the current window.
     */
    public int availableTokens() {
        lock.lock();
        try {
            evictExpired(Instant.now().toEpochMilli());
            return Math.max(0, maxTokens - tokenTimestamps.size());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns how many milliseconds until at least one token becomes available.
     * Returns 0 if a token is already available.
     */
    public long millisUntilNextToken() {
        lock.lock();
        try {
            long now = Instant.now().toEpochMilli();
            evictExpired(now);
            if (tokenTimestamps.size() < maxTokens) return 0L;
            long oldest = tokenTimestamps.peekFirst();
            return Math.max(0L, oldest + windowMillis - now);
        } finally {
            lock.unlock();
        }
    }

    public int getMaxTokens() { return maxTokens; }
    public long getWindowMillis() { return windowMillis; }

    private void evictExpired(long now) {
        long cutoff = now - windowMillis;
        while (!tokenTimestamps.isEmpty() && tokenTimestamps.peekFirst() <= cutoff) {
            tokenTimestamps.pollFirst();
        }
    }
}
