package com.apkdeltapush.priority;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a push job with an associated priority level and metadata.
 * Higher priority values indicate more urgent jobs.
 */
public class PrioritizedPushJob {

    public static final int PRIORITY_LOW = 1;
    public static final int PRIORITY_NORMAL = 5;
    public static final int PRIORITY_HIGH = 10;
    public static final int PRIORITY_CRITICAL = 20;

    private final String jobId;
    private final String deviceSerial;
    private final String apkPath;
    private final int priority;
    private final Instant enqueuedAt;

    public PrioritizedPushJob(String jobId, String deviceSerial, String apkPath, int priority) {
        if (jobId == null || jobId.isBlank()) throw new IllegalArgumentException("jobId must not be blank");
        if (deviceSerial == null || deviceSerial.isBlank()) throw new IllegalArgumentException("deviceSerial must not be blank");
        if (apkPath == null || apkPath.isBlank()) throw new IllegalArgumentException("apkPath must not be blank");
        if (priority < 0) throw new IllegalArgumentException("priority must be non-negative");
        this.jobId = jobId;
        this.deviceSerial = deviceSerial;
        this.apkPath = apkPath;
        this.priority = priority;
        this.enqueuedAt = Instant.now();
    }

    public String getJobId() { return jobId; }
    public String getDeviceSerial() { return deviceSerial; }
    public String getApkPath() { return apkPath; }
    public int getPriority() { return priority; }
    public Instant getEnqueuedAt() { return enqueuedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrioritizedPushJob)) return false;
        PrioritizedPushJob that = (PrioritizedPushJob) o;
        return Objects.equals(jobId, that.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }

    @Override
    public String toString() {
        return "PrioritizedPushJob{jobId='" + jobId + "', device='" + deviceSerial +
               "', priority=" + priority + ", enqueuedAt=" + enqueuedAt + "}";
    }
}
