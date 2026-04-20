package com.apkdeltapush.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PushAuditLoggerTest {

    private PushAuditLogger logger;

    @BeforeEach
    void setUp() {
        logger = new PushAuditLogger();
    }

    @Test
    void logAndRetrieveAllRecords() {
        logger.log("s1", "emulator-5554", "com.example.app", AuditEventType.PUSH_STARTED);
        logger.log("s1", "emulator-5554", "com.example.app", AuditEventType.PUSH_COMPLETED, "v2.0");

        List<PushAuditRecord> all = logger.getAllRecords();
        assertEquals(2, all.size());
    }

    @Test
    void filterBySession() {
        logger.log("session-A", "dev1", "com.foo", AuditEventType.PUSH_STARTED);
        logger.log("session-B", "dev2", "com.bar", AuditEventType.PUSH_STARTED);
        logger.log("session-A", "dev1", "com.foo", AuditEventType.PUSH_COMPLETED);

        List<PushAuditRecord> forA = logger.getRecordsForSession("session-A");
        assertEquals(2, forA.size());
        assertTrue(forA.stream().allMatch(r -> "session-A".equals(r.getSessionId())));
    }

    @Test
    void filterByDevice() {
        logger.log("s1", "device-X", "com.pkg", AuditEventType.PUSH_STARTED);
        logger.log("s2", "device-Y", "com.pkg", AuditEventType.PUSH_FAILED, "timeout");
        logger.log("s3", "device-X", "com.pkg", AuditEventType.PUSH_COMPLETED);

        List<PushAuditRecord> forX = logger.getRecordsForDevice("device-X");
        assertEquals(2, forX.size());
    }

    @Test
    void recordContainsExpectedFields() {
        logger.log("s1", "emu-5556", "com.test", AuditEventType.DELTA_APPLIED, "patch-v3");

        PushAuditRecord record = logger.getAllRecords().get(0);
        assertEquals("s1",              record.getSessionId());
        assertEquals("emu-5556",        record.getDeviceSerial());
        assertEquals("com.test",        record.getPackageName());
        assertEquals(AuditEventType.DELTA_APPLIED, record.getEventType());
        assertEquals("patch-v3",        record.getDetail());
        assertNotNull(record.getTimestamp());
    }

    @Test
    void clearRemovesAllRecords() {
        logger.log("s1", "dev", "pkg", AuditEventType.PUSH_STARTED);
        logger.clear();
        assertEquals(0, logger.size());
        assertTrue(logger.getAllRecords().isEmpty());
    }

    @Test
    void nullDetailDefaultsToEmptyString() {
        logger.log("s1", "dev", "pkg", AuditEventType.PUSH_STARTED);
        PushAuditRecord record = logger.getAllRecords().get(0);
        assertEquals("", record.getDetail());
    }

    @Test
    void unknownSessionReturnsEmptyList() {
        logger.log("s1", "dev", "pkg", AuditEventType.PUSH_STARTED);
        assertTrue(logger.getRecordsForSession("no-such-session").isEmpty());
    }
}
