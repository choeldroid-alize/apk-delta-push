package com.apkdeltapush.revert;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable result of a push revert operation.
 */
public final class RevertResult {

    private final String deviceSerial;
    private final String packageName;
    private final boolean success;
    private final String revertedToVersion;
    private final String errorMessage;
    private final Instant timestamp;

    private RevertResult(String deviceSerial, String packageName, boolean success,
                         String revertedToVersion, String errorMessage) {
        this.deviceSerial = Objects.requireNonNull(deviceSerial);
        this.packageName = Objects.requireNonNull(packageName);
        this.success = success;
        this.revertedToVersion = revertedToVersion;
        this.errorMessage = errorMessage;
        this.timestamp = Instant.now();
    }

    public static RevertResult success(String deviceSerial, String packageName, String revertedToVersion) {
        return new RevertResult(deviceSerial, packageName, true, revertedToVersion, null);
    }

    public static RevertResult failure(String deviceSerial, String packageName, String errorMessage) {
        return new RevertResult(deviceSerial, packageName, false, null, errorMessage);
    }

    public String getDeviceSerial() { return deviceSerial; }
    public String getPackageName() { return packageName; }
    public boolean isSuccess() { return success; }
    public String getRevertedToVersion() { return revertedToVersion; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        if (success) {
            return String.format("RevertResult{device='%s', package='%s', success=true, version='%s', at=%s}",
                    deviceSerial, packageName, revertedToVersion, timestamp);
        }
        return String.format("RevertResult{device='%s', package='%s', success=false, error='%s', at=%s}",
                deviceSerial, packageName, errorMessage, timestamp);
    }
}
