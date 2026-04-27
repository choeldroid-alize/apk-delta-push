package com.apkdeltapush.diff;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Schedules and manages asynchronous delta diff operations with
 * configurable concurrency and cancellation support.
 */
public class DeltaDiffScheduler {

    private static final Logger logger = Logger.getLogger(DeltaDiffScheduler.class.getName());

    private final ExecutorService executor;
    private final int maxConcurrentDiffs;
    private final Semaphore semaphore;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    public DeltaDiffScheduler(int maxConcurrentDiffs) {
        if (maxConcurrentDiffs <= 0) {
            throw new IllegalArgumentException("maxConcurrentDiffs must be positive");
        }
        this.maxConcurrentDiffs = maxConcurrentDiffs;
        this.semaphore = new Semaphore(maxConcurrentDiffs);
        this.executor = Executors.newFixedThreadPool(maxConcurrentDiffs, r -> {
            Thread t = new Thread(r, "delta-diff-scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Submits a diff task for asynchronous execution.
     *
     * @param task the diff task to execute
     * @return a Future representing the pending result
     * @throws RejectedExecutionException if the scheduler has been shut down
     */
    public Future<DeltaDiffResult> submit(DeltaDiffTask task) {
        if (shutdown.get()) {
            throw new RejectedExecutionException("DeltaDiffScheduler has been shut down");
        }
        return executor.submit(() -> {
            semaphore.acquire();
            try {
                logger.fine("Executing diff task: " + task.getTaskId());
                return task.execute();
            } finally {
                semaphore.release();
            }
        });
    }

    /**
     * Returns the number of currently available diff slots.
     */
    public int availableSlots() {
        return semaphore.availablePermits();
    }

    /**
     * Returns the configured maximum concurrent diffs.
     */
    public int getMaxConcurrentDiffs() {
        return maxConcurrentDiffs;
    }

    /**
     * Shuts down the scheduler, waiting up to the given timeout for tasks to complete.
     */
    public boolean shutdown(long timeoutMs) throws InterruptedException {
        shutdown.set(true);
        executor.shutdown();
        return executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Immediately shuts down, cancelling pending tasks.
     */
    public void shutdownNow() {
        shutdown.set(true);
        executor.shutdownNow();
    }

    public boolean isShutdown() {
        return shutdown.get();
    }
}
