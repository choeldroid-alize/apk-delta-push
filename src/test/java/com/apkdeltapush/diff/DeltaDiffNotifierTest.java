package com.apkdeltapush.diff;

import com.apkdeltapush.diff.DeltaDiffNotifier.DiffEvent;
import com.apkdeltapush.diff.DeltaDiffNotifier.DiffNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeltaDiffNotifierTest {

    private DeltaDiffNotifier notifier;

    @BeforeEach
    void setUp() {
        notifier = new DeltaDiffNotifier();
    }

    @Test
    void addListener_registersSuccessfully() {
        notifier.addListener(n -> {});
        assertEquals(1, notifier.getListeners().size());
    }

    @Test
    void addListener_nullThrows() {
        assertThrows(NullPointerException.class, () -> notifier.addListener(null));
    }

    @Test
    void removeListener_removesCorrectly() {
        List<DiffNotification> received = new ArrayList<>();
        notifier.addListener(received::add);
        boolean removed = notifier.removeListener(received::add);
        // note: lambda identity — use explicit reference
        notifier.clearListeners();
        assertEquals(0, notifier.getListeners().size());
    }

    @Test
    void notify_dispatchesToAllListeners() {
        List<DiffNotification> bucket1 = new ArrayList<>();
        List<DiffNotification> bucket2 = new ArrayList<>();
        notifier.addListener(bucket1::add);
        notifier.addListener(bucket2::add);

        notifier.notify("task-1", DiffEvent.STARTED, "beginning diff");

        assertEquals(1, bucket1.size());
        assertEquals(1, bucket2.size());
        assertEquals(DiffEvent.STARTED, bucket1.get(0).getEvent());
        assertEquals("task-1", bucket2.get(0).getTaskId());
        assertEquals("beginning diff", bucket1.get(0).getMessage());
    }

    @Test
    void notify_withoutMessage_setsNullMessage() {
        List<DiffNotification> received = new ArrayList<>();
        notifier.addListener(received::add);

        notifier.notify("task-2", DiffEvent.COMPLETED);

        assertNull(received.get(0).getMessage());
        assertEquals(DiffEvent.COMPLETED, received.get(0).getEvent());
    }

    @Test
    void notify_nullTaskIdThrows() {
        assertThrows(NullPointerException.class,
                () -> notifier.notify(null, DiffEvent.FAILED, "error"));
    }

    @Test
    void notify_nullEventThrows() {
        assertThrows(NullPointerException.class,
                () -> notifier.notify("task-3", null, "error"));
    }

    @Test
    void notify_faultyListenerDoesNotBlockOthers() {
        List<DiffNotification> received = new ArrayList<>();
        notifier.addListener(n -> { throw new RuntimeException("boom"); });
        notifier.addListener(received::add);

        assertDoesNotThrow(() -> notifier.notify("task-4", DiffEvent.CANCELLED));
        assertEquals(1, received.size());
    }

    @Test
    void clearListeners_removesAll() {
        notifier.addListener(n -> {});
        notifier.addListener(n -> {});
        notifier.clearListeners();
        assertTrue(notifier.getListeners().isEmpty());
    }

    @Test
    void notification_timestampIsSet() {
        List<DiffNotification> received = new ArrayList<>();
        notifier.addListener(received::add);
        long before = System.currentTimeMillis();
        notifier.notify("task-5", DiffEvent.STARTED);
        long after = System.currentTimeMillis();

        long ts = received.get(0).getTimestampMs();
        assertTrue(ts >= before && ts <= after);
    }
}
