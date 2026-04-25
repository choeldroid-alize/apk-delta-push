package com.apkdeltapush.window;

import java.time.Duration;

/**
 * Configuration for the sliding transfer window used to control
 * in-flight delta chunk transmissions over ADB.
 */
public class TransferWindowConfig {

    private final int maxWindowSize;
    private final int initialWindowSize;
    private final Duration ackTimeout;
    private final boolean adaptiveScaling;
    private final int scaleStepSize;

    private TransferWindowConfig(Builder builder) {
        this.maxWindowSize = builder.maxWindowSize;
        this.initialWindowSize = builder.initialWindowSize;
        this.ackTimeout = builder.ackTimeout;
        this.adaptiveScaling = builder.adaptiveScaling;
        this.scaleStepSize = builder.scaleStepSize;
    }

    public int getMaxWindowSize() { return maxWindowSize; }
    public int getInitialWindowSize() { return initialWindowSize; }
    public Duration getAckTimeout() { return ackTimeout; }
    public boolean isAdaptiveScaling() { return adaptiveScaling; }
    public int getScaleStepSize() { return scaleStepSize; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int maxWindowSize = 16;
        private int initialWindowSize = 4;
        private Duration ackTimeout = Duration.ofSeconds(5);
        private boolean adaptiveScaling = true;
        private int scaleStepSize = 2;

        public Builder maxWindowSize(int v) { this.maxWindowSize = v; return this; }
        public Builder initialWindowSize(int v) { this.initialWindowSize = v; return this; }
        public Builder ackTimeout(Duration v) { this.ackTimeout = v; return this; }
        public Builder adaptiveScaling(boolean v) { this.adaptiveScaling = v; return this; }
        public Builder scaleStepSize(int v) { this.scaleStepSize = v; return this; }

        public TransferWindowConfig build() {
            if (initialWindowSize > maxWindowSize) {
                throw new IllegalArgumentException(
                    "initialWindowSize must not exceed maxWindowSize");
            }
            return new TransferWindowConfig(this);
        }
    }

    @Override
    public String toString() {
        return String.format(
            "TransferWindowConfig{max=%d, initial=%d, ackTimeout=%s, adaptive=%b, step=%d}",
            maxWindowSize, initialWindowSize, ackTimeout, adaptiveScaling, scaleStepSize);
    }
}
