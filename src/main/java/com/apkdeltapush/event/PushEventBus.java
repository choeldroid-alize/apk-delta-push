package com.apkdeltapush.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A simple synchronous event bus for broadcasting push lifecycle events
 * to registered subscribers within the apk-delta-push pipeline.
 */
public class PushEventBus {

    private final Map<PushEventCategory, List<PushEventSubscriber>> subscribers = new ConcurrentHashMap<>();

    /**
     * Subscribe a listener to a specific event category.
     *
     * @param category   the category of events to listen for
     * @param subscriber the subscriber to notify
     */
    public void subscribe(PushEventCategory category, PushEventSubscriber subscriber) {
        if (category == null || subscriber == null) {
            throw new IllegalArgumentException("Category and subscriber must not be null");
        }
        subscribers
                .computeIfAbsent(category, k -> new CopyOnWriteArrayList<>())
                .add(subscriber);
    }

    /**
     * Unsubscribe a listener from a specific event category.
     *
     * @param category   the category to unsubscribe from
     * @param subscriber the subscriber to remove
     */
    public void unsubscribe(PushEventCategory category, PushEventSubscriber subscriber) {
        List<PushEventSubscriber> list = subscribers.get(category);
        if (list != null) {
            list.remove(subscriber);
        }
    }

    /**
     * Publish an event to all subscribers registered for its category.
     *
     * @param event the event to publish
     */
    public void publish(BusEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        List<PushEventSubscriber> list = subscribers.getOrDefault(event.getCategory(), Collections.emptyList());
        for (PushEventSubscriber subscriber : list) {
            subscriber.onEvent(event);
        }
    }

    /**
     * Returns the number of subscribers registered for a given category.
     *
     * @param category the event category
     * @return subscriber count
     */
    public int subscriberCount(PushEventCategory category) {
        List<PushEventSubscriber> list = subscribers.get(category);
        return list == null ? 0 : list.size();
    }

    /**
     * Clears all subscribers for all categories.
     */
    public void clearAll() {
        subscribers.clear();
    }

    /**
     * Returns an unmodifiable snapshot of all registered categories.
     */
    public List<PushEventCategory> registeredCategories() {
        return Collections.unmodifiableList(new ArrayList<>(subscribers.keySet()));
    }
}
