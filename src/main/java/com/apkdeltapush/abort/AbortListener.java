package com.apkdeltapush.abort;

/**
 * Callback interface for components that need to react to a push abort event.
 */
@FunctionalInterface
public interface AbortListener {

    /**
     * Called when the push operation is aborted.
     *
     * @param reason the reason the push was aborted
     */
    void onAbort(AbortReason reason);
}
