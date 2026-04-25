package com.apkdeltapush.diff;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable report summarizing the outcome of a delta diff operation,
 * including size statistics, strategy used, and any warnings raised.
 */
public final class DeltaDiffReport {

    private final String sourceApkPath;
    private final String targetApkPath;
    private final long sourceSizeBytes;
    private final long targetSizeBytes;
    private final long deltaSizeBytes;
    private final double compressionRatio;
    private final DeltaDiffStrategy strategy;
    private final long durationMillis;
    private final Instant generatedAt;
    private final List<String> warnings;
    private final boolean success;

    public DeltaDiffReport(
            String sourceApkPath,
            String targetApkPath,
            long sourceSizeBytes,
            long targetSizeBytes,
            long deltaSizeBytes,
            DeltaDiffStrategy strategy,
            long durationMillis,
            List<String> warnings,
            boolean success) {
        this.sourceApkPath = Objects.requireNonNull(sourceApkPath, "sourceApkPath");
        this.targetApkPath = Objects.requireNonNull(targetApkPath, "targetApkPath");
        this.sourceSizeBytes = sourceSizeBytes;
        this.targetSizeBytes = targetSizeBytes;
        this.deltaSizeBytes = deltaSizeBytes;
        this.compressionRatio = targetSizeBytes > 0
                ? 1.0 - ((double) deltaSizeBytes / targetSizeBytes)
                : 0.0;
        this.strategy = Objects.requireNonNull(strategy, "strategy");
        this.durationMillis = durationMillis;
        this.generatedAt = Instant.now();
        this.warnings = Collections.unmodifiableList(Objects.requireNonNull(warnings, "warnings"));
        this.success = success;
    }

    public String getSourceApkPath() { return sourceApkPath; }
    public String getTargetApkPath() { return targetApkPath; }
    public long getSourceSizeBytes() { return sourceSizeBytes; }
    public long getTargetSizeBytes() { return targetSizeBytes; }
    public long getDeltaSizeBytes() { return deltaSizeBytes; }
    public double getCompressionRatio() { return compressionRatio; }
    public DeltaDiffStrategy getStrategy() { return strategy; }
    public long getDurationMillis() { return durationMillis; }
    public Instant getGeneratedAt() { return generatedAt; }
    public List<String> getWarnings() { return warnings; }
    public boolean isSuccess() { return success; }
    public boolean hasWarnings() { return !warnings.isEmpty(); }

    @Override
    public String toString() {
        return String.format(
                "DeltaDiffReport{strategy=%s, delta=%d bytes, ratio=%.2f%%, duration=%dms, success=%b, warnings=%d}",
                strategy, deltaSizeBytes, compressionRatio * 100, durationMillis, success, warnings.size());
    }
}
