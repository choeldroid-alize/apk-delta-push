package com.apkdeltapush.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PushEventBusTest {

    private PushEventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new PushEventBus();
    }

    @Test
    void subscribe_andPublish_notifiesSubscriber() {
        PushEventSubscriber subscriber = mock(PushEventSubscriber.class);
        BusEvent event = new BusEvent(PushEventCategory.TRANSFER, "transfer-started", "device-001");

        eventBus.subscribe(PushEventCategory.TRANSFER, subscriber);
        eventBus.publish(event);

        verify(subscriber, times(1)).onEvent(event);
    }

    @Test
    void publish_doesNotNotifySubscriberOfDifferentCategory() {
        PushEventSubscriber subscriber = mock(PushEventSubscriber.class);
        BusEvent event = new BusEvent(PushEventCategory.INSTALL, "install-complete", "device-002");

        eventBus.subscribe(PushEventCategory.TRANSFER, subscriber);
        eventBus.publish(event);

        verify(subscriber, never()).onEvent(any());
    }

    @Test
    void unsubscribe_preventsNotification() {
        PushEventSubscriber subscriber = mock(PushEventSubscriber.class);
        BusEvent event = new BusEvent(PushEventCategory.TRANSFER, "transfer-started", "device-003");

        eventBus.subscribe(PushEventCategory.TRANSFER, subscriber);
        eventBus.unsubscribe(PushEventCategory.TRANSFER, subscriber);
        eventBus.publish(event);

        verify(subscriber, never()).onEvent(any());
    }

    @Test
    void subscriberCount_returnsCorrectCount() {
        PushEventSubscriber s1 = mock(PushEventSubscriber.class);
        PushEventSubscriber s2 = mock(PushEventSubscriber.class);

        eventBus.subscribe(PushEventCategory.INSTALL, s1);
        eventBus.subscribe(PushEventCategory.INSTALL, s2);

        assertEquals(2, eventBus.subscriberCount(PushEventCategory.INSTALL));
        assertEquals(0, eventBus.subscriberCount(PushEventCategory.TRANSFER));
    }

    @Test
    void clearAll_removesAllSubscribers() {
        PushEventSubscriber subscriber = mock(PushEventSubscriber.class);
        eventBus.subscribe(PushEventCategory.TRANSFER, subscriber);
        eventBus.subscribe(PushEventCategory.INSTALL, subscriber);

        eventBus.clearAll();

        assertEquals(0, eventBus.subscriberCount(PushEventCategory.TRANSFER));
        assertEquals(0, eventBus.subscriberCount(PushEventCategory.INSTALL));
        assertTrue(eventBus.registeredCategories().isEmpty());
    }

    @Test
    void publish_multipleSubscribers_allNotified() {
        List<BusEvent> received = new ArrayList<>();
        PushEventSubscriber s1 = received::add;
        PushEventSubscriber s2 = received::add;
        BusEvent event = new BusEvent(PushEventCategory.TRANSFER, "chunk-sent", "device-004");

        eventBus.subscribe(PushEventCategory.TRANSFER, s1);
        eventBus.subscribe(PushEventCategory.TRANSFER, s2);
        eventBus.publish(event);

        assertEquals(2, received.size());
    }

    @Test
    void subscribe_nullCategory_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> eventBus.subscribe(null, mock(PushEventSubscriber.class)));
    }

    @Test
    void publish_nullEvent_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> eventBus.publish(null));
    }
}
