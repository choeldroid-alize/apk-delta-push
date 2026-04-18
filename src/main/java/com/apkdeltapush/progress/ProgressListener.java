package com.apkdeltapush.progress;

/**
 * Listener interface for receiving push progress events.
 */
public interface ProgressListener {

    /**
     * Called when progress advances within a phase.
     *
     * @param phase            current phase
     * @param percentComplete  0-100
     * @param bytesTransferred bytes transferred so far
     * @param totalBytes       total bytes expected
     */
    void onProgress(PushPhase phase, int percentComplete, long bytesTransferred, long totalBytes);

    /**
     * Called when the operation moves to a new phase.
     */
    void onPhaseChanged(PushPhase newPhase);

    /**
     * Called when the push completes successfully.
     */
    void onComplete();

    /**
     * Called when the push fails.
     *
     * @param reason human-readable failure description
     */
    void onFailure(String reason);
}
