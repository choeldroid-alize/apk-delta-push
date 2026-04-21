package com.apkdeltapush.delta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the manifest of a generated APK delta, capturing metadata
 * about source/target versions, chunk offsets, and integrity checksums.
 */
public class DeltaManifest {

    private final String sourceVersion;
    private final String targetVersion;
    private final String packageName;
    private final long deltaSize;
    private final String deltaChecksum;
    private final Map<String, Long> chunkOffsets;
    private final long createdAt;

    private DeltaManifest(Builder builder) {
        this.sourceVersion = Objects.requireNonNull(builder.sourceVersion, "sourceVersion must not be null");
        this.targetVersion = Objects.requireNonNull(builder.targetVersion, "targetVersion must not be null");
        this.packageName   = Objects.requireNonNull(builder.packageName,   "packageName must not be null");
        this.deltaSize     = builder.deltaSize;
        this.deltaChecksum = Objects.requireNonNull(builder.deltaChecksum, "deltaChecksum must not be null");
        this.chunkOffsets  = Collections.unmodifiableMap(new HashMap<>(builder.chunkOffsets));
        this.createdAt     = builder.createdAt;
    }

    public String getSourceVersion()       { return sourceVersion; }
    public String getTargetVersion()       { return targetVersion; }
    public String getPackageName()         { return packageName; }
    public long   getDeltaSize()           { return deltaSize; }
    public String getDeltaChecksum()       { return deltaChecksum; }
    public Map<String, Long> getChunkOffsets() { return chunkOffsets; }
    public long   getCreatedAt()           { return createdAt; }

    public boolean isUpgrade() {
        return !sourceVersion.equals(targetVersion);
    }

    @Override
    public String toString() {
        return String.format("DeltaManifest{pkg='%s', %s -> %s, size=%d, checksum='%s'}",
                packageName, sourceVersion, targetVersion, deltaSize, deltaChecksum);
    }

    // -----------------------------------------------------------------------

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String sourceVersion;
        private String targetVersion;
        private String packageName;
        private long   deltaSize;
        private String deltaChecksum;
        private final Map<String, Long> chunkOffsets = new HashMap<>();
        private long   createdAt = System.currentTimeMillis();

        public Builder sourceVersion(String v)  { this.sourceVersion = v; return this; }
        public Builder targetVersion(String v)  { this.targetVersion = v; return this; }
        public Builder packageName(String p)    { this.packageName   = p; return this; }
        public Builder deltaSize(long s)        { this.deltaSize     = s; return this; }
        public Builder deltaChecksum(String c)  { this.deltaChecksum = c; return this; }
        public Builder createdAt(long ts)       { this.createdAt     = ts; return this; }
        public Builder addChunkOffset(String id, long offset) {
            this.chunkOffsets.put(id, offset);
            return this;
        }
        public DeltaManifest build() { return new DeltaManifest(this); }
    }
}
