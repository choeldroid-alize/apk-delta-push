package com.apkdeltapush.abort;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles graceful abort of an in-progress APK delta push operation.
 * Notifies registered listeners and performs cleanup.
 */
public class PushAbortHandler {

    private static final Logger logger = Logger.getLogger(PushAbortHandler.class.getName());

    private final List<AbortListener> listeners = new ArrayList<>();
    private volatile boolean aborted = false;
    private AbortReason lastReason = null;

    public void registerListener(AbortListener listener) {
        if (listener == null) throw new IllegalArgumentException("Listener must not be null");
        listeners.add(listener);
    }

    public void abort(AbortReason reason) {
        if (aborted) {
            logger.warning("Abort already triggered; ignoring duplicate abort with reason: " + reason);
            return;
        }
        aborted = true;
        lastReason = reason;
        logger.info("Push abort triggered. Reason: " + reason);
        for (AbortListener listener : listeners) {
            try {
                listener.onAbort(reason);
            } catch (Exception e) {
                logger.warning("AbortListener threw exception: " + e.getMessage());
            }
        }
    }

    public boolean isAborted() {
        return aborted;
    }

    public AbortReason getLastReason() {
        return lastReason;
    }

    public void reset() {
        aborted = false;
        lastReason = null;
    }
}
