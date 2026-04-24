package com.apkdeltapush.diff;

import java.util.Objects;

/**
 * Immutable configuration options passed to {@link ApkDiffGenerator}.
 */
public final class DeltaDiffOptions {

    private final DeltaDiffStrategy strategy;
    private final int compressionLevel;   // 1–9, applicable to BSDIFF / ZIP_ENTRY
    private final boolean validateOutput; // run checksum on produced delta
    private final long maxDeltaSizeBytes; // 0 = unlimited

    private DeltaDiffOptions(Builder builder) {
        this.strategy = builder.strategy;
        this.compressionLevel = builder.compressionLevel;
        this.validateOutput = builder.validateOutput;
        this.maxDeltaSizeBytes = builder.maxDeltaSizeBytes;
    }

    public DeltaDiffStrategy getStrategy() {
        return strategy;
    }

    public int getCompressionLevel() {
        return compressionLevel;
    }

    public boolean isValidateOutput() {
        return validateOutput;
    }

    public long getMaxDeltaSizeBytes() {
        return maxDeltaSizeBytes;
    }

    @Override
    public String toString() {
        return "DeltaDiffOptions{strategy=" + strategy
                + ", compressionLevel=" + compressionLevel
                + ", validateOutput=" + validateOutput
                + ", maxDeltaSizeBytes=" + maxDeltaSizeBytes + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private DeltaDiffStrategy strategy = DeltaDiffStrategy.AUTO;
        private int compressionLevel = 6;
        private boolean validateOutput = true;
        private long maxDeltaSizeBytes = 0;

        public Builder strategy(DeltaDiffStrategy strategy) {
            this.strategy = Objects.requireNonNull(strategy, "strategy");
            return this;
        }

        public Builder compressionLevel(int level) {
            if (level < 1 || level > 9) throw new IllegalArgumentException("compressionLevel must be 1-9");
            this.compressionLevel = level;
            return this;
        }

        public Builder validateOutput(boolean validate) {
            this.validateOutput = validate;
            return this;
        }

        public Builder maxDeltaSizeBytes(long max) {
            this.maxDeltaSizeBytes = max;
            return this;
        }

        public DeltaDiffOptions build() {
            return new DeltaDiffOptions(this);
        }
    }
}
