package com.apkdeltapush.batch;

/**
 * Functional interface for executing a single {@link BatchPushJob}
 * and returning a {@link BatchPushResult}.
 */
@FunctionalInterface
public interface BatchJobExecutor {
    BatchPushResult execute(BatchPushJob job);
}
