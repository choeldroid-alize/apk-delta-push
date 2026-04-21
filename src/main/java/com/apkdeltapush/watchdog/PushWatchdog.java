package com.apkdeltapush.watchdog;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Monitors an ongoing push operation and triggers a callback if it stalls
 * beyond the configured threshold.
 */
public class PushWatchdog {

    private final long stallThresholdMs;
    private final WatchdogListener listener;
    private final ScheduledExecutorService scheduler;

    private volatile Instant lastHeartbeat;
    private ScheduledFuture<?> watchTask;
    private final AtomicBoolean active = new AtomicBoolean(false);

    public PushWatchdog(long stallThresholdMs, WatchdogListener listener) {
        if (stallThresholdMs <= 0) {
            throw new IllegalArgumentException("Stall threshold must be positive");
        }
        this.stallThresholdMs = stallThresholdMs;
        this.listener = listener;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "push-watchdog");
            t.setDaemon(true);
            return t;
        });
    }

    public synchronized void start(String sessionId) {
        if (active.getAndSet(true)) {
            throw new IllegalStateException("Watchdog already running for session: " + sessionId);
        }
        lastHeartbeat = Instant.now();
        long intervalMs = Math.max(1000, stallThresholdMs / 4);
        watchTask = scheduler.scheduleAtFixedRate(() -> checkHeartbeat(sessionId),
                intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    public void heartbeat() {
        if (active.get()) {
            lastHeartbeat = Instant.now();
        }
    }

    public synchronized void stop() {
        if (active.getAndSet(false) && watchTask != null) {
            watchTask.cancel(false);
            watchTask = null;
        }
    }

    public void shutdown() {
        stop();
        scheduler.shutdownNow();
    }

    public boolean isActive() {
        return active.get();
    }

    private void checkHeartbeat(String sessionId) {
        Instant last = lastHeartbeat;
        if (last != null) {
            long elapsed = Instant.now().toEpochMilli() - last.toEpochMilli();
            if (elapsed >= stallThresholdMs) {
                WatchdogEvent event = new WatchdogEvent(sessionId, elapsed, stallThresholdMs);
                listener.onStallDetected(event);
            }
        }
    }
}
