package com.apkdeltapush.watchdog;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class PushWatchdogTest {

    private List<WatchdogEvent> capturedEvents;
    private PushWatchdog watchdog;

    @BeforeEach
    void setUp() {
        capturedEvents = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        if (watchdog != null) {
            watchdog.shutdown();
        }
    }

    @Test
    void stall_detected_when_no_heartbeat() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        watchdog = new PushWatchdog(200, event -> {
            capturedEvents.add(event);
            latch.countDown();
        });

        watchdog.start("session-1");
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Stall event should fire");
        assertFalse(capturedEvents.isEmpty());
        assertEquals("session-1", capturedEvents.get(0).getSessionId());
        assertTrue(capturedEvents.get(0).getStalledForMs() >= 200);
    }

    @Test
    void no_stall_when_heartbeats_are_sent() throws InterruptedException {
        watchdog = new PushWatchdog(500, event -> capturedEvents.add(event));
        watchdog.start("session-2");

        for (int i = 0; i < 6; i++) {
            Thread.sleep(100);
            watchdog.heartbeat();
        }

        watchdog.stop();
        assertTrue(capturedEvents.isEmpty(), "No stall should be detected with regular heartbeats");
    }

    @Test
    void start_throws_if_already_running() {
        watchdog = new PushWatchdog(1000, event -> {});
        watchdog.start("session-3");
        assertThrows(IllegalStateException.class, () -> watchdog.start("session-3"));
    }

    @Test
    void constructor_rejects_non_positive_threshold() {
        assertThrows(IllegalArgumentException.class, () -> new PushWatchdog(0, event -> {}));
        assertThrows(IllegalArgumentException.class, () -> new PushWatchdog(-100, event -> {}));
    }

    @Test
    void is_not_active_after_stop() {
        watchdog = new PushWatchdog(1000, event -> {});
        watchdog.start("session-4");
        assertTrue(watchdog.isActive());
        watchdog.stop();
        assertFalse(watchdog.isActive());
    }
}
