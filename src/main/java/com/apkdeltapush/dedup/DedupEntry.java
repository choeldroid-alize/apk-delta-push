package com.apkdeltapush.dedup;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable value object representing a cached delta entry in the
 * deduplication index.
 */
public final class DedupEntry {

    private final String sourceHash;
    private final String targetHash;
    private final String deltaPath;
    private final long   deltaSize;
    private final Instant createdAt;

    public DedupEntry(String sourceHash, String targetHash, String deltaPath, long deltaSize) {
        this.sourceHash = Objects.requireNonNull(sourceHash, "sourceHash must not be null");
        this.targetHash = Objects.requireNonNull(targetHash, "targetHash must not be null");
        this.deltaPath  = Objects.requireNonNull(deltaPath,  "deltaPath must not be null");
        if (deltaSize < 0) throw new IllegalArgumentException("deltaSize must be >= 0");
        this.deltaSize  = deltaSize;
        this.createdAt  = Instant.now();
    }

    public String getSourceHash() { return sourceHash; }
    public String getTargetHash() { return targetHash; }
    public String getDeltaPath()  { return deltaPath; }
    public long   getDeltaSize()  { return deltaSize; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DedupEntry)) return false;
        DedupEntry that = (DedupEntry) o;
        return deltaSize == that.deltaSize
            && sourceHash.equals(that.sourceHash)
            && targetHash.equals(that.targetHash)
            && deltaPath.equals(that.deltaPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceHash, targetHash, deltaPath, deltaSize);
    }

    @Override
    public String toString() {
        return "DedupEntry{sourceHash='" + sourceHash + "', targetHash='" + targetHash
            + "', deltaPath='" + deltaPath + "', deltaSize=" + deltaSize
            + ", createdAt=" + createdAt + '}';
    }
}
