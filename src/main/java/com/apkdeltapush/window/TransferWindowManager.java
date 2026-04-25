package com.apkdeltapush.window;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Manages a sliding window of in-flight delta chunk transfers.
 * Supports adaptive window scaling based on ACK feedback.
 */
public class TransferWindowManager {

    private static final Logger log = Logger.getLogger(TransferWindowManager.class.getName());

    private final TransferWindowConfig config;
    private final AtomicInteger currentWindowSize;
    private volatile Semaphore windowSemaphore;
    private final AtomicInteger consecutiveAcks = new AtomicInteger(0);
    private final AtomicInteger consecutiveNacks = new AtomicInteger(0);

    public TransferWindowManager(TransferWindowConfig config) {
        this.config = config;
        this.currentWindowSize = new AtomicInteger(config.getInitialWindowSize());
        this.windowSemaphore = new Semaphore(config.getInitialWindowSize(), true);
    }

    /**
     * Acquires a slot in the transfer window, blocking until one is available
     * or the configured ACK timeout elapses.
     *
     * @return true if a slot was acquired, false if timed out
     */
    public boolean acquireSlot() throws InterruptedException {
        long timeoutMs = config.getAckTimeout().toMillis();
        boolean acquired = windowSemaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        if (!acquired) {
            log.warning("Transfer window slot acquire timed out after " + timeoutMs + "ms");
        }
        return acquired;
    }

    /**
     * Releases a slot back to the window and optionally expands the window.
     */
    public void acknowledgeChunk() {
        windowSemaphore.release();
        if (config.isAdaptiveScaling()) {
            int acks = consecutiveAcks.incrementAndGet();
            consecutiveNacks.set(0);
            if (acks >= config.getScaleStepSize()) {
                expandWindow();
                consecutiveAcks.set(0);
            }
        }
    }

    /**
     * Releases a slot and shrinks the window on negative acknowledgement.
     */
    public void negativeAcknowledgeChunk() {
        windowSemaphore.release();
        if (config.isAdaptiveScaling()) {
            consecutiveNacks.incrementAndGet();
            consecutiveAcks.set(0);
            shrinkWindow();
        }
    }

    public int getCurrentWindowSize() {
        return currentWindowSize.get();
    }

    public void reset() {
        int initial = config.getInitialWindowSize();
        currentWindowSize.set(initial);
        windowSemaphore = new Semaphore(initial, true);
        consecutiveAcks.set(0);
        consecutiveNacks.set(0);
        log.info("Transfer window reset to initial size: " + initial);
    }

    private synchronized void expandWindow() {
        int current = currentWindowSize.get();
        int next = Math.min(current + config.getScaleStepSize(), config.getMaxWindowSize());
        if (next > current) {
            windowSemaphore.release(next - current);
            currentWindowSize.set(next);
            log.fine("Transfer window expanded: " + current + " -> " + next);
        }
    }

    private synchronized void shrinkWindow() {
        int current = currentWindowSize.get();
        int next = Math.max(current - config.getScaleStepSize(), 1);
        if (next < current) {
            currentWindowSize.set(next);
            log.fine("Transfer window shrunk: " + current + " -> " + next);
        }
    }
}
