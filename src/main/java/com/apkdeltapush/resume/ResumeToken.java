package com.apkdeltapush.resume;

import java.time.Instant;

/**
 * Represents a resumable push token storing transfer state.
 */
public class ResumeToken {
    private final String deviceId;
    private final String packageName;
    private final long bytesTransferred;
    private final long totalBytes;
    private final String patchChecksum;
    private final Instant createdAt;

    public ResumeToken(String deviceId, String packageName, long bytesTransferred,
                       long totalBytes, String patchChecksum) {
        this.deviceId = deviceId;
        this.packageName = packageName;
        this.bytesTransferred = bytesTransferred;
        this.totalBytes = totalBytes;
        this.patchChecksum = patchChecksum;
        this.createdAt = Instant.now();
    }

    public String getDeviceId() { return deviceId; }
    public String getPackageName() { return packageName; }
    public long getBytesTransferred() { return bytesTransferred; }
    public long getTotalBytes() { return totalBytes; }
    public String getPatchChecksum() { return patchChecksum; }
    public Instant getCreatedAt() { return createdAt; }

    public double getProgressPercent() {
        if (totalBytes == 0) return 0.0;
        return (bytesTransferred * 100.0) / totalBytes;
    }

    public boolean isComplete() {
        return bytesTransferred >= totalBytes;
    }

    @Override
    public String toString() {
        return String.format("ResumeToken[device=%s, pkg=%s, progress=%.1f%%]",
                deviceId, packageName, getProgressPercent());
    }
}
