package com.apkdeltapush.watchdog;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class WatchdogEventTest {

    @Test
    void fields_are_populated_correctly() {
        Instant before = Instant.now();
        WatchdogEvent event = new WatchdogEvent("sess-42", 3000L, 2000L);
        Instant after = Instant.now();

        assertEquals("sess-42", event.getSessionId());
        assertEquals(3000L, event.getStalledForMs());
        assertEquals(2000L, event.getThresholdMs());
        assertNotNull(event.getDetectedAt());
        assertFalse(event.getDetectedAt().isBefore(before));
        assertFalse(event.getDetectedAt().isAfter(after));
    }

    @Test
    void toString_contains_session_id() {
        WatchdogEvent event = new WatchdogEvent("sess-99", 1500L, 1000L);
        assertTrue(event.toString().contains("sess-99"));
        assertTrue(event.toString().contains("1500"));
    }
}
