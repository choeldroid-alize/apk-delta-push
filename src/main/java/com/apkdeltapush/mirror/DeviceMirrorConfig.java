package com.apkdeltapush.mirror;

import java.util.Objects;

/**
 * Configuration for mirroring an APK push to multiple target devices.
 */
public class DeviceMirrorConfig {

    private final String sourceDeviceId;
    private final java.util.List<String> targetDeviceIds;
    private final boolean failFast;
    private final boolean skipIdenticalVersions;
    private final int maxConcurrentMirrors;

    private DeviceMirrorConfig(Builder builder) {
        this.sourceDeviceId = Objects.requireNonNull(builder.sourceDeviceId, "sourceDeviceId must not be null");
        this.targetDeviceIds = java.util.Collections.unmodifiableList(
                new java.util.ArrayList<>(Objects.requireNonNull(builder.targetDeviceIds, "targetDeviceIds must not be null")));
        this.failFast = builder.failFast;
        this.skipIdenticalVersions = builder.skipIdenticalVersions;
        this.maxConcurrentMirrors = builder.maxConcurrentMirrors;
    }

    public String getSourceDeviceId() { return sourceDeviceId; }
    public java.util.List<String> getTargetDeviceIds() { return targetDeviceIds; }
    public boolean isFailFast() { return failFast; }
    public boolean isSkipIdenticalVersions() { return skipIdenticalVersions; }
    public int getMaxConcurrentMirrors() { return maxConcurrentMirrors; }

    public static Builder builder(String sourceDeviceId) {
        return new Builder(sourceDeviceId);
    }

    public static final class Builder {
        private final String sourceDeviceId;
        private java.util.List<String> targetDeviceIds = new java.util.ArrayList<>();
        private boolean failFast = false;
        private boolean skipIdenticalVersions = true;
        private int maxConcurrentMirrors = 4;

        private Builder(String sourceDeviceId) {
            this.sourceDeviceId = sourceDeviceId;
        }

        public Builder targetDeviceIds(java.util.List<String> ids) { this.targetDeviceIds = ids; return this; }
        public Builder failFast(boolean failFast) { this.failFast = failFast; return this; }
        public Builder skipIdenticalVersions(boolean skip) { this.skipIdenticalVersions = skip; return this; }
        public Builder maxConcurrentMirrors(int max) { this.maxConcurrentMirrors = max; return this; }

        public DeviceMirrorConfig build() { return new DeviceMirrorConfig(this); }
    }

    @Override
    public String toString() {
        return "DeviceMirrorConfig{source=" + sourceDeviceId +
               ", targets=" + targetDeviceIds +
               ", failFast=" + failFast +
               ", skipIdentical=" + skipIdenticalVersions +
               ", maxConcurrent=" + maxConcurrentMirrors + "}";
    }
}
