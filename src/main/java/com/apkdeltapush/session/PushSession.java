package com.apkdeltapush.session;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single APK delta push session, tracking state and metadata.
 */
public class PushSession {

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }

    private final String sessionId;
    private final String deviceSerial;
    private final String packageName;
    private final Instant createdAt;
    private Instant completedAt;
    private Status status;
    private String errorMessage;
    private long bytesPushed;

    public PushSession(String deviceSerial, String packageName) {
        this.sessionId = UUID.randomUUID().toString();
        this.deviceSerial = deviceSerial;
        this.packageName = packageName;
        this.createdAt = Instant.now();
        this.status = Status.PENDING;
        this.bytesPushed = 0;
    }

    public void start() {
        this.status = Status.IN_PROGRESS;
    }

    public void complete(long bytesPushed) {
        this.status = Status.COMPLETED;
        this.bytesPushed = bytesPushed;
        this.completedAt = Instant.now();
    }

    public void fail(String errorMessage) {
        this.status = Status.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
    }

    public String getSessionId() { return sessionId; }
    public String getDeviceSerial() { return deviceSerial; }
    public String getPackageName() { return packageName; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Status getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public long getBytesPushed() { return bytesPushed; }

    @Override
    public String toString() {
        return String.format("PushSession{id=%s, device=%s, pkg=%s, status=%s}",
                sessionId, deviceSerial, packageName, status);
    }
}
