package com.apkdeltapush.notify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PushNotificationDispatcherTest {

    private PushNotificationDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new PushNotificationDispatcher();
    }

    @Test
    void testRegisterAndDispatch() {
        List<PushNotification> received = new ArrayList<>();
        dispatcher.registerListener(received::add);

        PushNotification n = new PushNotification("emulator-5554", "com.example",
                PushNotificationType.PUSH_STARTED, "Starting push");
        dispatcher.dispatch(n);

        assertEquals(1, received.size());
        assertEquals(PushNotificationType.PUSH_STARTED, received.get(0).getType());
    }

    @Test
    void testUnregisterListener() {
        List<PushNotification> received = new ArrayList<>();
        PushNotificationListener listener = received::add;
        dispatcher.registerListener(listener);
        dispatcher.unregisterListener(listener);

        dispatcher.dispatch(new PushNotification("emulator-5554", "com.example",
                PushNotificationType.PUSH_COMPLETED, "Done"));

        assertTrue(received.isEmpty());
    }

    @Test
    void testDispatchAllNotifications() {
        List<PushNotification> received = new ArrayList<>();
        dispatcher.registerListener(received::add);

        List<PushNotification> toSend = List.of(
                new PushNotification("d1", "com.app", PushNotificationType.PUSH_STARTED, "start"),
                new PushNotification("d1", "com.app", PushNotificationType.PATCH_APPLIED, "patched"),
                new PushNotification("d1", "com.app", PushNotificationType.PUSH_COMPLETED, "done")
        );
        dispatcher.dispatchAll(toSend);

        assertEquals(3, received.size());
    }

    @Test
    void testListenerExceptionDoesNotHaltDispatch() {
        List<PushNotification> received = new ArrayList<>();
        dispatcher.registerListener(n -> { throw new RuntimeException("boom"); });
        dispatcher.registerListener(received::add);

        dispatcher.dispatch(new PushNotification("d1", "com.app",
                PushNotificationType.PUSH_FAILED, "fail"));

        assertEquals(1, received.size());
    }

    @Test
    void testClearListeners() {
        dispatcher.registerListener(n -> {});
        dispatcher.registerListener(n -> {});
        assertEquals(2, dispatcher.listenerCount());
        dispatcher.clearListeners();
        assertEquals(0, dispatcher.listenerCount());
    }

    @Test
    void testRegisterNullListenerIgnored() {
        dispatcher.registerListener(null);
        assertEquals(0, dispatcher.listenerCount());
    }
}
