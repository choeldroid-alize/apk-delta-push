package com.apkdeltapush.history;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable record representing a single APK push operation stored in history.
 */
public class PushHistoryRecord {

    private final String recordId;
    private final String deviceSerial;
    private final String packageName;
    private final String fromVersion;
    private final String toVersion;
    private final long deltaBytes;
    private final boolean success;
    private final String failureReason;
    private final Instant timestamp;
    private final long durationMillis;

    public PushHistoryRecord(String recordId, String deviceSerial, String packageName,
                             String fromVersion, String toVersion, long deltaBytes,
                             boolean success, String failureReason,
                             Instant timestamp, long durationMillis) {
        this.recordId = Objects.requireNonNull(recordId, "recordId must not be null");
        this.deviceSerial = Objects.requireNonNull(deviceSerial, "deviceSerial must not be null");
        this.packageName = Objects.requireNonNull(packageName, "packageName must not be null");
        this.fromVersion = fromVersion;
        this.toVersion = Objects.requireNonNull(toVersion, "toVersion must not be null");
        if (deltaBytes < 0) {
            throw new IllegalArgumentException("deltaBytes must not be negative: " + deltaBytes);
        }
        this.deltaBytes = deltaBytes;
        this.success = success;
        this.failureReason = failureReason;
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        if (durationMillis < 0) {
            throw new IllegalArgumentException("durationMillis must not be negative: " + durationMillis);
        }
        this.durationMillis = durationMillis;
    }

    public String getRecordId() { return recordId; }
    public String getDeviceSerial() { return deviceSerial; }
    public String getPackageName() { return packageName; }
    public String getFromVersion() { return fromVersion; }
    public String getToVersion() { return toVersion; }
    public long getDeltaBytes() { return deltaBytes; }
    public boolean isSuccess() { return success; }
    public String getFailureReason() { return failureReason; }
    public Instant getTimestamp() { return timestamp; }
    public long getDurationMillis() { return durationMillis; }

    @Override
    public String toString() {
        return String.format("PushHistoryRecord{id='%s', device='%s', pkg='%s', %s->%s, success=%b, ts=%s}",
                recordId, deviceSerial, packageName, fromVersion, toVersion, success, timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PushHistoryRecord)) return false;
        PushHistoryRecord that = (PushHistoryRecord) o;
        return Objects.equals(recordId, that.recordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId);
    }
}
