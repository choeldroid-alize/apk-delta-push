package com.apkdeltapush.schedule;

import java.util.Objects;

/**
 * Represents a scheduled delta push job.
 */
public class PushJob {

    private final String jobId;
    private final String deviceSerial;
    private final String apkPath;
    private final Runnable task;
    private JobStatus status;

    public PushJob(String jobId, String deviceSerial, String apkPath, Runnable task) {
        this.jobId = Objects.requireNonNull(jobId);
        this.deviceSerial = Objects.requireNonNull(deviceSerial);
        this.apkPath = Objects.requireNonNull(apkPath);
        this.task = Objects.requireNonNull(task);
        this.status = JobStatus.PENDING;
    }

    public String getJobId() { return jobId; }
    public String getDeviceSerial() { return deviceSerial; }
    public String getApkPath() { return apkPath; }
    public JobStatus getStatus() { return status; }

    public void execute() {
        status = JobStatus.RUNNING;
        try {
            task.run();
            status = JobStatus.COMPLETED;
        } catch (Exception e) {
            status = JobStatus.FAILED;
            throw e;
        }
    }

    @Override
    public String toString() {
        return "PushJob{jobId='" + jobId + "', device='" + deviceSerial + "', apk='" + apkPath + "', status=" + status + "}";
    }
}
