package com.apkdeltapush.schedule;

/**
 * Lifecycle states of a PushJob.
 */
public enum JobStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}
