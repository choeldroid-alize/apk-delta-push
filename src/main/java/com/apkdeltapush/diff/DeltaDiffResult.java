package com.apkdeltapush.diff;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents the result of a delta diff operation between two APK versions.
 */
public class DeltaDiffResult {

    private final Path deltaFile;
    private final long originalSize;
    private final long patchedSize;
    private final long deltaSize;
    private final DeltaDiffStrategy strategy;
    private final Instant generatedAt;
    private final boolean success;
    private final String errorMessage;

    private DeltaDiffResult(Builder builder) {
        this.deltaFile = builder.deltaFile;
        this.originalSize = builder.originalSize;
        this.patchedSize = builder.patchedSize;
        this.deltaSize = builder.deltaSize;
        this.strategy = builder.strategy;
        this.generatedAt = builder.generatedAt;
        this.success = builder.success;
        this.errorMessage = builder.errorMessage;
    }

    public Path getDeltaFile() { return deltaFile; }
    public long getOriginalSize() { return originalSize; }
    public long getPatchedSize() { return patchedSize; }
    public long getDeltaSize() { return deltaSize; }
    public DeltaDiffStrategy getStrategy() { return strategy; }
    public Instant getGeneratedAt() { return generatedAt; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }

    public double getCompressionRatio() {
        if (originalSize == 0) return 0.0;
        return 1.0 - ((double) deltaSize / originalSize);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Path deltaFile;
        private long originalSize;
        private long patchedSize;
        private long deltaSize;
        private DeltaDiffStrategy strategy;
        private Instant generatedAt = Instant.now();
        private boolean success;
        private String errorMessage;

        public Builder deltaFile(Path deltaFile) { this.deltaFile = deltaFile; return this; }
        public Builder originalSize(long originalSize) { this.originalSize = originalSize; return this; }
        public Builder patchedSize(long patchedSize) { this.patchedSize = patchedSize; return this; }
        public Builder deltaSize(long deltaSize) { this.deltaSize = deltaSize; return this; }
        public Builder strategy(DeltaDiffStrategy strategy) { this.strategy = strategy; return this; }
        public Builder generatedAt(Instant generatedAt) { this.generatedAt = Objects.requireNonNull(generatedAt); return this; }
        public Builder success(boolean success) { this.success = success; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }

        public DeltaDiffResult build() {
            return new DeltaDiffResult(this);
        }
    }

    @Override
    public String toString() {
        return String.format("DeltaDiffResult{success=%s, strategy=%s, deltaSize=%d, ratio=%.2f%%}",
                success, strategy, deltaSize, getCompressionRatio() * 100);
    }
}
