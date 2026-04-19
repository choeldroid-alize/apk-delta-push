package com.apkdeltapush.schedule;

import java.time.Instant;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Schedules delta push jobs for execution, supporting immediate and delayed runs.
 */
public class PushScheduler {

    private static final Logger LOG = Logger.getLogger(PushScheduler.class.getName());

    private final ScheduledExecutorService executor;
    private final ConcurrentMap<String, ScheduledFuture<?>> scheduled = new ConcurrentHashMap<>();

    public PushScheduler(int threadPoolSize) {
        this.executor = Executors.newScheduledThreadPool(threadPoolSize);
    }

    /**
     * Schedule a job to run after the given delay in seconds.
     */
    public void schedule(PushJob job, long delaySeconds) {
        if (scheduled.containsKey(job.getJobId())) {
            LOG.warning("Job already scheduled: " + job.getJobId());
            return;
        }
        ScheduledFuture<?> future = executor.schedule(() -> runJob(job), delaySeconds, TimeUnit.SECONDS);
        scheduled.put(job.getJobId(), future);
        LOG.info("Scheduled job " + job.getJobId() + " in " + delaySeconds + "s");
    }

    /**
     * Run a job immediately.
     */
    public void scheduleNow(PushJob job) {
        schedule(job, 0);
    }

    /**
     * Cancel a pending job by ID.
     */
    public boolean cancel(String jobId) {
        ScheduledFuture<?> future = scheduled.remove(jobId);
        if (future != null && !future.isDone()) {
            future.cancel(false);
            LOG.info("Cancelled job: " + jobId);
            return true;
        }
        return false;
    }

    public boolean isScheduled(String jobId) {
        ScheduledFuture<?> f = scheduled.get(jobId);
        return f != null && !f.isDone() && !f.isCancelled();
    }

    private void runJob(PushJob job) {
        scheduled.remove(job.getJobId());
        LOG.info("Running job: " + job.getJobId() + " at " + Instant.now());
        try {
            job.execute();
        } catch (Exception e) {
            LOG.severe("Job " + job.getJobId() + " failed: " + e.getMessage());
        }
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
