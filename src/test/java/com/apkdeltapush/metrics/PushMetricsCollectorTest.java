package com.apkdeltapush.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PushMetricsCollectorTest {

    private PushMetricsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new PushMetricsCollector("session-001");
    }

    @Test
    void constructor_rejectsBlankSessionId() {
        assertThrows(IllegalArgumentException.class, () -> new PushMetricsCollector(""));
        assertThrows(IllegalArgumentException.class, () -> new PushMetricsCollector(null));
    }

    @Test
    void recordBytesSent_accumulatesCorrectly() {
        collector.recordBytesSent("device-A", 500);
        collector.recordBytesSent("device-A", 300);
        collector.recordBytesSent("device-B", 200);
        PushMetricsSnapshot snap = collector.snapshot();
        assertEquals(1000, snap.getTotalBytesSent());
        assertEquals(800, snap.getPerDeviceBytesSent().get("device-A"));
        assertEquals(200, snap.getPerDeviceBytesSent().get("device-B"));
    }

    @Test
    void recordBytesSent_rejectsNegativeBytes() {
        assertThrows(IllegalArgumentException.class, () -> collector.recordBytesSent("device-A", -1));
    }

    @Test
    void recordBytesReceived_accumulatesCorrectly() {
        collector.recordBytesReceived(400);
        collector.recordBytesReceived(100);
        assertEquals(500, collector.snapshot().getTotalBytesReceived());
    }

    @Test
    void recordSuccessAndFailure_countsCorrectly() {
        collector.recordSuccess();
        collector.recordSuccess();
        collector.recordFailure();
        PushMetricsSnapshot snap = collector.snapshot();
        assertEquals(2, snap.getSuccessfulPushes());
        assertEquals(1, snap.getFailedPushes());
        assertEquals(3, snap.getTotalPushes());
    }

    @Test
    void successRate_calculatesCorrectly() {
        collector.recordSuccess();
        collector.recordSuccess();
        collector.recordFailure();
        double rate = collector.snapshot().getSuccessRate();
        assertEquals(66.66, rate, 0.01);
    }

    @Test
    void successRate_zeroWhenNoPushes() {
        assertEquals(0.0, collector.snapshot().getSuccessRate());
    }

    @Test
    void reset_clearsAllCounters() {
        collector.recordBytesSent("device-A", 1000);
        collector.recordSuccess();
        collector.reset();
        PushMetricsSnapshot snap = collector.snapshot();
        assertEquals(0, snap.getTotalBytesSent());
        assertEquals(0, snap.getSuccessfulPushes());
        assertTrue(snap.getPerDeviceBytesSent().isEmpty());
    }

    @Test
    void snapshot_sessionIdMatchesCollector() {
        assertEquals("session-001", collector.snapshot().getSessionId());
    }
}
