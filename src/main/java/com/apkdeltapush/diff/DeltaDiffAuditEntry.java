package com.apkdeltapush.diff;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single audit entry for a delta diff operation,
 * capturing metadata about what was diffed, when, and the outcome.
 */
public class DeltaDiffAuditEntry {

    private final String entryId;
    private final String sourceApkPath;
    private final String targetApkPath;
    private final long sourceSizeBytes;
    private final long targetSizeBytes;
    private final long deltaSizeBytes;
    private final Instant timestamp;
    private final boolean success;
    private final String failureReason;
    private final String strategyUsed;

    public DeltaDiffAuditEntry(String entryId, String sourceApkPath, String targetApkPath,
                                long sourceSizeBytes, long targetSizeBytes, long deltaSizeBytes,
                                Instant timestamp, boolean success, String failureReason,
                                String strategyUsed) {
        this.entryId = Objects.requireNonNull(entryId, "entryId must not be null");
        this.sourceApkPath = Objects.requireNonNull(sourceApkPath, "sourceApkPath must not be null");
        this.targetApkPath = Objects.requireNonNull(targetApkPath, "targetApkPath must not be null");
        this.sourceSizeBytes = sourceSizeBytes;
        this.targetSizeBytes = targetSizeBytes;
        this.deltaSizeBytes = deltaSizeBytes;
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.success = success;
        this.failureReason = failureReason;
        this.strategyUsed = strategyUsed;
    }

    public String getEntryId() { return entryId; }
    public String getSourceApkPath() { return sourceApkPath; }
    public String getTargetApkPath() { return targetApkPath; }
    public long getSourceSizeBytes() { return sourceSizeBytes; }
    public long getTargetSizeBytes() { return targetSizeBytes; }
    public long getDeltaSizeBytes() { return deltaSizeBytes; }
    public Instant getTimestamp() { return timestamp; }
    public boolean isSuccess() { return success; }
    public String getFailureReason() { return failureReason; }
    public String getStrategyUsed() { return strategyUsed; }

    public double getCompressionRatio() {
        if (targetSizeBytes == 0) return 0.0;
        return (double) deltaSizeBytes / (double) targetSizeBytes;
    }

    @Override
    public String toString() {
        return String.format("DeltaDiffAuditEntry{id='%s', success=%b, strategy='%s', ratio=%.2f, ts=%s}",
                entryId, success, strategyUsed, getCompressionRatio(), timestamp);
    }
}
