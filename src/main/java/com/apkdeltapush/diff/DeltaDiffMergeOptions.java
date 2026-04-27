package com.apkdeltapush.diff;

/**
 * Configuration options controlling how DeltaDiffMerger combines multiple diff results.
 */
public class DeltaDiffMergeOptions {

    /** Whether to fail the merged result if any individual result has warnings. */
    private boolean failOnWarnings;

    /** Whether to deduplicate identical warnings across merged results. */
    private boolean deduplicateWarnings;

    /** Maximum allowed total delta size (bytes) after merge; 0 = unlimited. */
    private long maxMergedDeltaSize;

    /** Label to attach to the merged result for traceability. */
    private String mergeLabel;

    public DeltaDiffMergeOptions() {
        this.failOnWarnings = false;
        this.deduplicateWarnings = true;
        this.maxMergedDeltaSize = 0;
        this.mergeLabel = "merged";
    }

    public boolean isFailOnWarnings() {
        return failOnWarnings;
    }

    public DeltaDiffMergeOptions setFailOnWarnings(boolean failOnWarnings) {
        this.failOnWarnings = failOnWarnings;
        return this;
    }

    public boolean isDeduplicateWarnings() {
        return deduplicateWarnings;
    }

    public DeltaDiffMergeOptions setDeduplicateWarnings(boolean deduplicateWarnings) {
        this.deduplicateWarnings = deduplicateWarnings;
        return this;
    }

    public long getMaxMergedDeltaSize() {
        return maxMergedDeltaSize;
    }

    public DeltaDiffMergeOptions setMaxMergedDeltaSize(long maxMergedDeltaSize) {
        if (maxMergedDeltaSize < 0) {
            throw new IllegalArgumentException("maxMergedDeltaSize must be >= 0");
        }
        this.maxMergedDeltaSize = maxMergedDeltaSize;
        return this;
    }

    public String getMergeLabel() {
        return mergeLabel;
    }

    public DeltaDiffMergeOptions setMergeLabel(String mergeLabel) {
        this.mergeLabel = mergeLabel;
        return this;
    }

    @Override
    public String toString() {
        return "DeltaDiffMergeOptions{" +
            "failOnWarnings=" + failOnWarnings +
            ", deduplicateWarnings=" + deduplicateWarnings +
            ", maxMergedDeltaSize=" + maxMergedDeltaSize +
            ", mergeLabel='" + mergeLabel + '\'' +
            '}';
    }
}
