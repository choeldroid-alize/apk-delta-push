package com.apkdeltapush.diagnostics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PushDiagnosticsCollectorTest {

    private PushDiagnosticsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new PushDiagnosticsCollector("session-001", "emulator-5554");
    }

    @Test
    void constructorRejectsBlankSessionId() {
        assertThrows(IllegalArgumentException.class,
                () -> new PushDiagnosticsCollector("", "emulator-5554"));
    }

    @Test
    void constructorRejectsBlankDeviceSerial() {
        assertThrows(IllegalArgumentException.class,
                () -> new PushDiagnosticsCollector("session-001", "  "));
    }

    @Test
    void recordStoresEntry() {
        collector.record("delta_size_bytes", "204800");
        assertEquals(1, collector.entryCount());
    }

    @Test
    void recordNullValueStoredAsNullPlaceholder() {
        collector.record("optional_field", null);
        PushDiagnosticsReport report = collector.buildReport();
        assertEquals("<null>", report.getEntry("optional_field"));
    }

    @Test
    void recordErrorSetsDegradedStatus() {
        collector.recordError("checksum_mismatch", "expected abc, got xyz");
        assertEquals(DiagnosticsStatus.DEGRADED, collector.getCurrentStatus());
    }

    @Test
    void markFailedSetsFailedStatus() {
        collector.markFailed("ADB connection lost");
        assertEquals(DiagnosticsStatus.FAILED, collector.getCurrentStatus());
        PushDiagnosticsReport report = collector.buildReport();
        assertEquals("ADB connection lost", report.getEntry("failure_reason"));
    }

    @Test
    void recordSystemInfoAddsMultipleEntries() {
        collector.recordSystemInfo();
        PushDiagnosticsReport report = collector.buildReport();
        assertTrue(report.hasEntry("os_name"));
        assertTrue(report.hasEntry("java_version"));
        assertTrue(report.hasEntry("available_processors"));
        assertTrue(report.hasEntry("free_memory_bytes"));
    }

    @Test
    void buildReportContainsCorrectMetadata() {
        collector.record("transfer_rate_kbps", "1024");
        PushDiagnosticsReport report = collector.buildReport();
        assertEquals("session-001", report.getSessionId());
        assertEquals("emulator-5554", report.getDeviceSerial());
        assertEquals(DiagnosticsStatus.OK, report.getStatus());
        assertNotNull(report.getCapturedAt());
    }

    @Test
    void reportEntriesAreImmutable() {
        collector.record("key", "value");
        PushDiagnosticsReport report = collector.buildReport();
        assertThrows(UnsupportedOperationException.class,
                () -> report.getEntries().put("extra", "data"));
    }

    @Test
    void recordBlankKeyThrows() {
        assertThrows(IllegalArgumentException.class, () -> collector.record("", "value"));
    }
}
