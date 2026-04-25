package com.apkdeltapush.diff;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Immutable summary of a delta diff operation, capturing key metrics
 * such as sizes, changed entry counts, and timing information.
 */
public final class DeltaDiffSummary {

    private final String sourceApkPath;
    private final String targetApkPath;
    private final long sourceSizeBytes;
    private final long targetSizeBytes;
    private final long deltaSizeBytes;
    private final int totalEntries;
    private final int changedEntries;
    private final int addedEntries;
    private final int removedEntries;
    private final Instant generatedAt;
    private final long durationMillis;
    private final List<String> changedEntryNames;

    private DeltaDiffSummary(Builder builder) {
        this.sourceApkPath   = builder.sourceApkPath;
        this.targetApkPath   = builder.targetApkPath;
        this.sourceSizeBytes = builder.sourceSizeBytes;
        this.targetSizeBytes = builder.targetSizeBytes;
        this.deltaSizeBytes  = builder.deltaSizeBytes;
        this.totalEntries    = builder.totalEntries;
        this.changedEntries  = builder.changedEntries;
        this.addedEntries    = builder.addedEntries;
        this.removedEntries  = builder.removedEntries;
        this.generatedAt     = builder.generatedAt;
        this.durationMillis  = builder.durationMillis;
        this.changedEntryNames = Collections.unmodifiableList(builder.changedEntryNames);
    }

    public String getSourceApkPath()     { return sourceApkPath; }
    public String getTargetApkPath()     { return targetApkPath; }
    public long getSourceSizeBytes()     { return sourceSizeBytes; }
    public long getTargetSizeBytes()     { return targetSizeBytes; }
    public long getDeltaSizeBytes()      { return deltaSizeBytes; }
    public int getTotalEntries()         { return totalEntries; }
    public int getChangedEntries()       { return changedEntries; }
    public int getAddedEntries()         { return addedEntries; }
    public int getRemovedEntries()       { return removedEntries; }
    public Instant getGeneratedAt()      { return generatedAt; }
    public long getDurationMillis()      { return durationMillis; }
    public List<String> getChangedEntryNames() { return changedEntryNames; }

    /** Reduction ratio: delta size relative to target size (0.0 – 1.0). */
    public double getReductionRatio() {
        if (targetSizeBytes == 0) return 0.0;
        return 1.0 - ((double) deltaSizeBytes / targetSizeBytes);
    }

    @Override
    public String toString() {
        return String.format(
            "DeltaDiffSummary{source='%s', target='%s', deltaBytes=%d, changed=%d, added=%d, removed=%d, reductionRatio=%.2f}",
            sourceApkPath, targetApkPath, deltaSizeBytes,
            changedEntries, addedEntries, removedEntries, getReductionRatio());
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String sourceApkPath = "";
        private String targetApkPath = "";
        private long sourceSizeBytes;
        private long targetSizeBytes;
        private long deltaSizeBytes;
        private int totalEntries;
        private int changedEntries;
        private int addedEntries;
        private int removedEntries;
        private Instant generatedAt = Instant.now();
        private long durationMillis;
        private List<String> changedEntryNames = Collections.emptyList();

        public Builder sourceApkPath(String v)   { this.sourceApkPath = v; return this; }
        public Builder targetApkPath(String v)   { this.targetApkPath = v; return this; }
        public Builder sourceSizeBytes(long v)   { this.sourceSizeBytes = v; return this; }
        public Builder targetSizeBytes(long v)   { this.targetSizeBytes = v; return this; }
        public Builder deltaSizeBytes(long v)    { this.deltaSizeBytes = v; return this; }
        public Builder totalEntries(int v)       { this.totalEntries = v; return this; }
        public Builder changedEntries(int v)     { this.changedEntries = v; return this; }
        public Builder addedEntries(int v)       { this.addedEntries = v; return this; }
        public Builder removedEntries(int v)     { this.removedEntries = v; return this; }
        public Builder generatedAt(Instant v)    { this.generatedAt = v; return this; }
        public Builder durationMillis(long v)    { this.durationMillis = v; return this; }
        public Builder changedEntryNames(List<String> v) { this.changedEntryNames = v; return this; }

        public DeltaDiffSummary build() { return new DeltaDiffSummary(this); }
    }
}
