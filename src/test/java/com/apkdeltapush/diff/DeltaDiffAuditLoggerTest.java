package com.apkdeltapush.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeltaDiffAuditLoggerTest {

    private DeltaDiffAuditLogger logger;

    @BeforeEach
    void setUp() {
        logger = new DeltaDiffAuditLogger(50);
    }

    @Test
    void recordCreatesEntryWithGeneratedId() {
        DeltaDiffAuditEntry entry = logger.record("/old.apk", "/new.apk",
                10_000L, 12_000L, 3_000L, true, null, "bsdiff");
        assertNotNull(entry.getEntryId());
        assertFalse(entry.getEntryId().isEmpty());
        assertTrue(entry.isSuccess());
        assertEquals("bsdiff", entry.getStrategyUsed());
    }

    @Test
    void getAllEntriesReturnsAllRecorded() {
        logger.record("/a.apk", "/b.apk", 1000L, 1100L, 200L, true, null, "bsdiff");
        logger.record("/c.apk", "/d.apk", 2000L, 2200L, 400L, false, "IO error", "xdelta");
        List<DeltaDiffAuditEntry> all = logger.getAllEntries();
        assertEquals(2, all.size());
    }

    @Test
    void getFailedEntriesFiltersCorrectly() {
        logger.record("/a.apk", "/b.apk", 1000L, 1100L, 200L, true, null, "bsdiff");
        logger.record("/c.apk", "/d.apk", 2000L, 2200L, 400L, false, "timeout", "xdelta");
        logger.record("/e.apk", "/f.apk", 500L, 600L, 100L, false, "corrupt", "bsdiff");
        List<DeltaDiffAuditEntry> failed = logger.getFailedEntries();
        assertEquals(2, failed.size());
        assertTrue(failed.stream().noneMatch(DeltaDiffAuditEntry::isSuccess));
    }

    @Test
    void getEntriesForSourceFiltersCorrectly() {
        logger.record("/app.apk", "/v2.apk", 1000L, 1100L, 200L, true, null, "bsdiff");
        logger.record("/app.apk", "/v3.apk", 1100L, 1200L, 150L, true, null, "bsdiff");
        logger.record("/other.apk", "/v2.apk", 500L, 600L, 80L, true, null, "xdelta");
        List<DeltaDiffAuditEntry> results = logger.getEntriesForSource("/app.apk");
        assertEquals(2, results.size());
    }

    @Test
    void findByIdReturnsCorrectEntry() {
        DeltaDiffAuditEntry recorded = logger.record("/x.apk", "/y.apk",
                3000L, 3500L, 700L, true, null, "xdelta");
        Optional<DeltaDiffAuditEntry> found = logger.findById(recorded.getEntryId());
        assertTrue(found.isPresent());
        assertEquals(recorded.getEntryId(), found.get().getEntryId());
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        Optional<DeltaDiffAuditEntry> found = logger.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void averageCompressionRatioCalculatedForSuccessfulEntries() {
        logger.record("/a.apk", "/b.apk", 1000L, 1000L, 500L, true, null, "bsdiff");  // ratio 0.5
        logger.record("/c.apk", "/d.apk", 1000L, 1000L, 250L, true, null, "bsdiff");  // ratio 0.25
        logger.record("/e.apk", "/f.apk", 1000L, 1000L, 800L, false, "err", "bsdiff"); // excluded
        double avg = logger.getAverageCompressionRatio();
        assertEquals(0.375, avg, 0.001);
    }

    @Test
    void maxEntriesLimitTrimsOldestEntries() {
        DeltaDiffAuditLogger smallLogger = new DeltaDiffAuditLogger(3);
        for (int i = 0; i < 5; i++) {
            smallLogger.record("/a" + i + ".apk", "/b" + i + ".apk",
                    1000L, 1100L, 200L, true, null, "bsdiff");
        }
        assertEquals(3, smallLogger.getTotalEntryCount());
    }

    @Test
    void clearRemovesAllEntries() {
        logger.record("/a.apk", "/b.apk", 1000L, 1100L, 200L, true, null, "bsdiff");
        logger.clear();
        assertEquals(0, logger.getTotalEntryCount());
        assertTrue(logger.getAllEntries().isEmpty());
    }

    @Test
    void constructorRejectsNonPositiveMaxEntries() {
        assertThrows(IllegalArgumentException.class, () -> new DeltaDiffAuditLogger(0));
        assertThrows(IllegalArgumentException.class, () -> new DeltaDiffAuditLogger(-5));
    }
}
