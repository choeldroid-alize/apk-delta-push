package com.apkdeltapush.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages batch push operations across multiple devices or APKs.
 * Tracks per-job results and provides aggregate reporting.
 */
public class BatchPushManager {

    private final List<BatchPushJob> pendingJobs = new CopyOnWriteArrayList<>();
    private final List<BatchPushResult> completedResults = new CopyOnWriteArrayList<>();
    private volatile boolean running = false;

    public void addJob(BatchPushJob job) {
        if (job == null) throw new IllegalArgumentException("Job must not be null");
        pendingJobs.add(job);
    }

    public void clearJobs() {
        pendingJobs.clear();
    }

    public List<BatchPushJob> getPendingJobs() {
        return Collections.unmodifiableList(pendingJobs);
    }

    public List<BatchPushResult> getCompletedResults() {
        return Collections.unmodifiableList(completedResults);
    }

    public void executeBatch(BatchJobExecutor executor) {
        if (executor == null) throw new IllegalArgumentException("Executor must not be null");
        running = true;
        completedResults.clear();
        List<BatchPushJob> snapshot = new ArrayList<>(pendingJobs);
        for (BatchPushJob job : snapshot) {
            if (!running) break;
            BatchPushResult result = executor.execute(job);
            completedResults.add(result);
        }
        running = false;
    }

    public void abort() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public BatchSummary summarize() {
        int total = completedResults.size();
        long successCount = completedResults.stream().filter(BatchPushResult::isSuccess).count();
        long failureCount = total - successCount;
        return new BatchSummary(total, (int) successCount, (int) failureCount);
    }
}
