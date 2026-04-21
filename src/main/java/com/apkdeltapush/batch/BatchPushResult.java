package com.apkdeltapush.batch;

import java.util.Objects;

/**
 * Holds the outcome of executing a single {@link BatchPushJob}.
 */
public class BatchPushResult {

    private final BatchPushJob job;
    private final boolean success;
    private final String message;
    private final long durationMs;

    public BatchPushResult(BatchPushJob job, boolean success, String message, long durationMs) {
        this.job = Objects.requireNonNull(job, "job must not be null");
        this.success = success;
        this.message = message != null ? message : "";
        this.durationMs = durationMs;
    }

    public static BatchPushResult ok(BatchPushJob job, long durationMs) {
        return new BatchPushResult(job, true, "OK", durationMs);
    }

    public static BatchPushResult failure(BatchPushJob job, String reason, long durationMs) {
        return new BatchPushResult(job, false, reason, durationMs);
    }

    public BatchPushJob getJob() { return job; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public long getDurationMs() { return durationMs; }

    @Override
    public String toString() {
        return "BatchPushResult{jobId='" + job.getJobId() + "', success=" + success + ", message='" + message + "', durationMs=" + durationMs + "}";
    }
}
