package com.apkdeltapush.parallel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Coordinates parallel APK delta pushes to multiple devices simultaneously,
 * managing thread pools and aggregating results.
 */
public class ParallelPushCoordinator {

    private final int maxParallelism;
    private final ExecutorService executorService;
    private final AtomicInteger activeCount = new AtomicInteger(0);

    public ParallelPushCoordinator(int maxParallelism) {
        if (maxParallelism < 1) {
            throw new IllegalArgumentException("maxParallelism must be at least 1");
        }
        this.maxParallelism = maxParallelism;
        this.executorService = Executors.newFixedThreadPool(maxParallelism);
    }

    /**
     * Submits push tasks for all given device IDs and returns a map of
     * device ID -> ParallelPushResult once all tasks complete.
     */
    public Map<String, ParallelPushResult> pushToAll(
            List<String> deviceIds,
            PushTaskFactory taskFactory) throws InterruptedException {

        if (deviceIds == null || deviceIds.isEmpty()) {
            return Map.of();
        }

        Map<String, Future<ParallelPushResult>> futures = new ConcurrentHashMap<>();
        for (String deviceId : deviceIds) {
            Callable<ParallelPushResult> task = () -> {
                activeCount.incrementAndGet();
                try {
                    return taskFactory.createTask(deviceId).call();
                } finally {
                    activeCount.decrementAndGet();
                }
            };
            futures.put(deviceId, executorService.submit(task));
        }

        Map<String, ParallelPushResult> results = new ConcurrentHashMap<>();
        for (Map.Entry<String, Future<ParallelPushResult>> entry : futures.entrySet()) {
            try {
                results.put(entry.getKey(), entry.getValue().get());
            } catch (ExecutionException e) {
                results.put(entry.getKey(),
                        ParallelPushResult.failure(entry.getKey(), e.getCause() != null
                                ? e.getCause().getMessage() : e.getMessage()));
            }
        }
        return results;
    }

    public int getActiveCount() {
        return activeCount.get();
    }

    public int getMaxParallelism() {
        return maxParallelism;
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public boolean isShutdown() {
        return executorService.isShutdown();
    }
}
