package com.apkdeltapush.checkpoint;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a checkpoint captured during a push operation,
 * allowing the push to be resumed from a known-good state.
 */
public class PushCheckpoint {

    private final String checkpointId;
    private final String sessionId;
    private final String deviceSerial;
    private final String packageName;
    private final long bytesTransferred;
    private final int fragmentIndex;
    private final CheckpointPhase phase;
    private final Instant capturedAt;

    public PushCheckpoint(String checkpointId, String sessionId, String deviceSerial,
                          String packageName, long bytesTransferred, int fragmentIndex,
                          CheckpointPhase phase, Instant capturedAt) {
        this.checkpointId = Objects.requireNonNull(checkpointId, "checkpointId must not be null");
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId must not be null");
        this.deviceSerial = Objects.requireNonNull(deviceSerial, "deviceSerial must not be null");
        this.packageName = Objects.requireNonNull(packageName, "packageName must not be null");
        this.bytesTransferred = bytesTransferred;
        this.fragmentIndex = fragmentIndex;
        this.phase = Objects.requireNonNull(phase, "phase must not be null");
        this.capturedAt = Objects.requireNonNull(capturedAt, "capturedAt must not be null");
    }

    public String getCheckpointId() { return checkpointId; }
    public String getSessionId() { return sessionId; }
    public String getDeviceSerial() { return deviceSerial; }
    public String getPackageName() { return packageName; }
    public long getBytesTransferred() { return bytesTransferred; }
    public int getFragmentIndex() { return fragmentIndex; }
    public CheckpointPhase getPhase() { return phase; }
    public Instant getCapturedAt() { return capturedAt; }

    @Override
    public String toString() {
        return "PushCheckpoint{" +
                "checkpointId='" + checkpointId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", deviceSerial='" + deviceSerial + '\'' +
                ", packageName='" + packageName + '\'' +
                ", bytesTransferred=" + bytesTransferred +
                ", fragmentIndex=" + fragmentIndex +
                ", phase=" + phase +
                ", capturedAt=" + capturedAt +
                '}';
    }
}
