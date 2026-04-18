package com.apkdeltapush.progress;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks progress of an APK delta push operation.
 */
public class PushProgressTracker {

    private final long totalBytes;
    private final AtomicLong transferredBytes = new AtomicLong(0);
    private final ProgressListener listener;
    private volatile PushPhase currentPhase = PushPhase.INITIALIZING;

    public PushProgressTracker(long totalBytes, ProgressListener listener) {
        if (totalBytes <= 0) throw new IllegalArgumentException("totalBytes must be positive");
        this.totalBytes = totalBytes;
        this.listener = listener;
    }

    public void advance(long bytes) {
        long current = transferredBytes.addAndGet(bytes);
        int percent = (int) Math.min(100, (current * 100) / totalBytes);
        if (listener != null) {
            listener.onProgress(currentPhase, percent, current, totalBytes);
        }
    }

    public void setPhase(PushPhase phase) {
        this.currentPhase = phase;
        if (listener != null) {
            listener.onPhaseChanged(phase);
        }
    }

    public void complete() {
        transferredBytes.set(totalBytes);
        if (listener != null) {
            listener.onProgress(PushPhase.COMPLETE, 100, totalBytes, totalBytes);
            listener.onComplete();
        }
    }

    public void fail(String reason) {
        if (listener != null) {
            listener.onFailure(reason);
        }
    }

    public long getTransferredBytes() {
        return transferredBytes.get();
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public PushPhase getCurrentPhase() {
        return currentPhase;
    }
}
