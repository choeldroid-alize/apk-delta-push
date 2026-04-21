package com.apkdeltapush.ratelimit;

/**
 * Immutable configuration for the push rate limiter.
 */
public class RateLimitConfig {

    public static final RateLimitConfig DEFAULT = new RateLimitConfig(10, 1_000L, true);
    public static final RateLimitConfig UNLIMITED = new RateLimitConfig(Integer.MAX_VALUE, 1_000L, false);

    private final int maxPushesPerWindow;
    private final long windowMillis;
    private final boolean enforced;

    public RateLimitConfig(int maxPushesPerWindow, long windowMillis, boolean enforced) {
        if (maxPushesPerWindow <= 0) throw new IllegalArgumentException("maxPushesPerWindow must be > 0");
        if (windowMillis <= 0) throw new IllegalArgumentException("windowMillis must be > 0");
        this.maxPushesPerWindow = maxPushesPerWindow;
        this.windowMillis = windowMillis;
        this.enforced = enforced;
    }

    public int getMaxPushesPerWindow() { return maxPushesPerWindow; }
    public long getWindowMillis() { return windowMillis; }
    public boolean isEnforced() { return enforced; }

    public RateLimiter buildLimiter() {
        return new RateLimiter(maxPushesPerWindow, windowMillis);
    }

    @Override
    public String toString() {
        return "RateLimitConfig{maxPushesPerWindow=" + maxPushesPerWindow
                + ", windowMillis=" + windowMillis
                + ", enforced=" + enforced + "}";
    }
}
