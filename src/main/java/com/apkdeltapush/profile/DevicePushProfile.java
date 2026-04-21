package com.apkdeltapush.profile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a per-device push profile capturing device-specific
 * transfer preferences, constraints, and historical performance data.
 */
public class DevicePushProfile {

    private final String deviceId;
    private int maxChunkSizeBytes;
    private int preferredParallelStreams;
    private long averageTransferRateBytesPerSec;
    private boolean compressionEnabled;
    private boolean encryptionEnabled;
    private final Map<String, String> customAttributes;

    public DevicePushProfile(String deviceId) {
        this.deviceId = Objects.requireNonNull(deviceId, "deviceId must not be null");
        this.maxChunkSizeBytes = 512 * 1024; // 512 KB default
        this.preferredParallelStreams = 1;
        this.averageTransferRateBytesPerSec = 0L;
        this.compressionEnabled = true;
        this.encryptionEnabled = false;
        this.customAttributes = new HashMap<>();
    }

    public String getDeviceId() { return deviceId; }

    public int getMaxChunkSizeBytes() { return maxChunkSizeBytes; }
    public void setMaxChunkSizeBytes(int maxChunkSizeBytes) {
        if (maxChunkSizeBytes <= 0) throw new IllegalArgumentException("maxChunkSizeBytes must be positive");
        this.maxChunkSizeBytes = maxChunkSizeBytes;
    }

    public int getPreferredParallelStreams() { return preferredParallelStreams; }
    public void setPreferredParallelStreams(int streams) {
        if (streams < 1) throw new IllegalArgumentException("streams must be >= 1");
        this.preferredParallelStreams = streams;
    }

    public long getAverageTransferRateBytesPerSec() { return averageTransferRateBytesPerSec; }
    public void setAverageTransferRateBytesPerSec(long rate) { this.averageTransferRateBytesPerSec = rate; }

    public boolean isCompressionEnabled() { return compressionEnabled; }
    public void setCompressionEnabled(boolean compressionEnabled) { this.compressionEnabled = compressionEnabled; }

    public boolean isEncryptionEnabled() { return encryptionEnabled; }
    public void setEncryptionEnabled(boolean encryptionEnabled) { this.encryptionEnabled = encryptionEnabled; }

    public void setAttribute(String key, String value) { customAttributes.put(key, value); }
    public String getAttribute(String key) { return customAttributes.get(key); }
    public Map<String, String> getCustomAttributes() { return new HashMap<>(customAttributes); }

    @Override
    public String toString() {
        return "DevicePushProfile{deviceId='" + deviceId + "', chunkSize=" + maxChunkSizeBytes +
               ", streams=" + preferredParallelStreams + ", compression=" + compressionEnabled +
               ", encryption=" + encryptionEnabled + "}";
    }
}
