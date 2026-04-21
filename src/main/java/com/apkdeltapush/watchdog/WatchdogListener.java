package com.apkdeltapush.watchdog;

/**
 * Callback interface notified when the {@link PushWatchdog} detects a stalled push.
 */
public interface WatchdogListener {

    /**
     * Called when a push session has not produced a heartbeat within the
     * configured stall threshold.
     *
     * @param event details about the stall event
     */
    void onStallDetected(WatchdogEvent event);
}
