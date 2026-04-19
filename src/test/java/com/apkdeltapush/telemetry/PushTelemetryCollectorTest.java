package com.apkdeltapush.telemetry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PushTelemetryCollectorTest {

    private PushTelemetryCollector collector;

    @BeforeEach
    void setUp() {
        collector = new PushTelemetryCollector();
    }

    @Test
    void recordAndRetrieveEntries() {
        collector.record("device-1", "DIFF", 1024L, true);
        collector.record("device-1", "PUSH", 2048L, true);

        List<TelemetryEntry> entries = collector.getEntries("device-1");
        assertEquals(2, entries.size());
        assertEquals("DIFF", entries.get(0).getPhase());
        assertEquals(1024L, entries.get(0).getBytesTransferred());
    }

    @Test
    void summarizeCalculatesTotalsCorrectly() {
        collector.record("device-2", "DIFF", 500L, true);
        collector.record("device-2", "PUSH", 1500L, false);
        collector.record("device-2", "VERIFY", 100L, true);

        TelemetrySummary summary = collector.summarize("device-2");
        assertEquals("device-2", summary.getDeviceId());
        assertEquals(3, summary.getTotalOperations());
        assertEquals(2100L, summary.getTotalBytesTransferred());
        assertEquals(1, summary.getFailureCount());
        assertEquals(1.0 / 3.0, summary.getErrorRate(), 0.0001);
    }

    @Test
    void summarizeEmptyDeviceReturnsZeros() {
        TelemetrySummary summary = collector.summarize("unknown-device");
        assertEquals(0, summary.getTotalOperations());
        assertEquals(0L, summary.getTotalBytesTransferred());
        assertEquals(0.0, summary.getErrorRate(), 0.0001);
    }

    @Test
    void clearRemovesDeviceEntries() {
        collector.record("device-3", "PUSH", 800L, true);
        collector.clear("device-3");
        assertTrue(collector.getEntries("device-3").isEmpty());
    }

    @Test
    void clearAllRemovesAllEntries() {
        collector.record("device-4", "PUSH", 100L, true);
        collector.record("device-5", "PUSH", 200L, false);
        collector.clearAll();
        assertTrue(collector.getEntries("device-4").isEmpty());
        assertTrue(collector.getEntries("device-5").isEmpty());
    }

    @Test
    void getEntriesReturnsUnmodifiableList() {
        collector.record("device-6", "DIFF", 300L, true);
        List<TelemetryEntry> entries = collector.getEntries("device-6");
        assertThrows(UnsupportedOperationException.class, () -> entries.add(null));
    }
}
