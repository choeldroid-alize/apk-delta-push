package com.apkdeltapush.batch;

import java.util.Objects;

/**
 * Represents a single push job within a batch operation,
 * associating a device serial with an APK file path.
 */
public class BatchPushJob {

    private final String jobId;
    private final String deviceSerial;
    private final String apkPath;
    private final int priority;

    public BatchPushJob(String jobId, String deviceSerial, String apkPath, int priority) {
        if (jobId == null || jobId.isBlank()) throw new IllegalArgumentException("jobId must not be blank");
        if (deviceSerial == null || deviceSerial.isBlank()) throw new IllegalArgumentException("deviceSerial must not be blank");
        if (apkPath == null || apkPath.isBlank()) throw new IllegalArgumentException("apkPath must not be blank");
        this.jobId = jobId;
        this.deviceSerial = deviceSerial;
        this.apkPath = apkPath;
        this.priority = priority;
    }

    public String getJobId() { return jobId; }
    public String getDeviceSerial() { return deviceSerial; }
    public String getApkPath() { return apkPath; }
    public int getPriority() { return priority; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BatchPushJob)) return false;
        BatchPushJob that = (BatchPushJob) o;
        return Objects.equals(jobId, that.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }

    @Override
    public String toString() {
        return "BatchPushJob{jobId='" + jobId + "', device='" + deviceSerial + "', apk='" + apkPath + "', priority=" + priority + "}";
    }
}
