package com.apkdeltapush.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Dispatches notifications to registered listeners when delta diff lifecycle
 * events occur (started, completed, failed, cancelled).
 */
public class DeltaDiffNotifier {

    public enum DiffEvent {
        STARTED, COMPLETED, FAILED, CANCELLED
    }

    private final List<Consumer<DiffNotification>> listeners = new ArrayList<>();

    /** Register a listener to receive diff notifications. */
    public void addListener(Consumer<DiffNotification> listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        listeners.add(listener);
    }

    /** Remove a previously registered listener. */
    public boolean removeListener(Consumer<DiffNotification> listener) {
        return listeners.remove(listener);
    }

    /** Returns an unmodifiable view of current listeners. */
    public List<Consumer<DiffNotification>> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    /**
     * Notify all listeners of a diff event.
     *
     * @param taskId    identifier of the diff task
     * @param event     the lifecycle event
     * @param message   optional human-readable message (may be null)
     */
    public void notify(String taskId, DiffEvent event, String message) {
        Objects.requireNonNull(taskId, "taskId must not be null");
        Objects.requireNonNull(event, "event must not be null");
        DiffNotification notification = new DiffNotification(taskId, event, message);
        for (Consumer<DiffNotification> listener : listeners) {
            try {
                listener.accept(notification);
            } catch (Exception e) {
                // Isolate listener failures so other listeners still receive the event
                System.err.println("[DeltaDiffNotifier] Listener threw exception: " + e.getMessage());
            }
        }
    }

    /** Convenience overload without a message. */
    public void notify(String taskId, DiffEvent event) {
        notify(taskId, event, null);
    }

    /** Remove all registered listeners. */
    public void clearListeners() {
        listeners.clear();
    }

    // -------------------------------------------------------------------------
    // Notification value object
    // -------------------------------------------------------------------------

    public static final class DiffNotification {
        private final String taskId;
        private final DiffEvent event;
        private final String message;
        private final long timestampMs;

        public DiffNotification(String taskId, DiffEvent event, String message) {
            this.taskId = taskId;
            this.event = event;
            this.message = message;
            this.timestampMs = System.currentTimeMillis();
        }

        public String getTaskId()      { return taskId; }
        public DiffEvent getEvent()    { return event; }
        public String getMessage()     { return message; }
        public long getTimestampMs()   { return timestampMs; }

        @Override
        public String toString() {
            return "DiffNotification{taskId='" + taskId + "', event=" + event +
                    ", message='" + message + "', ts=" + timestampMs + "}";
        }
    }
}
