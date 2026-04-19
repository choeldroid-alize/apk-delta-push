package com.apkdeltapush.notify;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Dispatches push lifecycle notifications to registered listeners.
 */
public class PushNotificationDispatcher {

    private final List<PushNotificationListener> listeners = new CopyOnWriteArrayList<>();

    public void registerListener(PushNotificationListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(PushNotificationListener listener) {
        listeners.remove(listener);
    }

    public void dispatch(PushNotification notification) {
        if (notification == null) return;
        for (PushNotificationListener listener : listeners) {
            try {
                listener.onNotification(notification);
            } catch (Exception e) {
                // isolate listener failures
            }
        }
    }

    public void dispatchAll(List<PushNotification> notifications) {
        if (notifications == null) return;
        for (PushNotification n : notifications) {
            dispatch(n);
        }
    }

    public int listenerCount() {
        return listeners.size();
    }

    public void clearListeners() {
        listeners.clear();
    }
}
