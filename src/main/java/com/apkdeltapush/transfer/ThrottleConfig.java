package com.apkdeltapush.transfer;

/**
 * Immutable configuration for transfer throttling.
 */
public class ThrottleConfig {

    public static final ThrottleConfig UNLIMITED = new ThrottleConfig(Long.MAX_VALUE, false);

    private final long maxBytesPerSecond;
    private final boolean enabled;

    public ThrottleConfig(long maxBytesPerSecond, boolean enabled) {
        if (maxBytesPerSecond <= 0) {
            throw new IllegalArgumentException("maxBytesPerSecond must be positive");
        }
        this.maxBytesPerSecond = maxBytesPerSecond;
        this.enabled = enabled;
    }

    public static ThrottleConfig of(long maxBytesPerSecond) {
        return new ThrottleConfig(maxBytesPerSecond, true);
    }

    public long getMaxBytesPerSecond() {
        return maxBytesPerSecond;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public TransferThrottler createThrottler() {
        return new TransferThrottler(maxBytesPerSecond);
    }

    @Override
    public String toString() {
        return "ThrottleConfig{maxBytesPerSecond=" + maxBytesPerSecond + ", enabled=" + enabled + "}";
    }
}
