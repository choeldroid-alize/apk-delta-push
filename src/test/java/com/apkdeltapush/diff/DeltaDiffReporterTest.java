package com.apkdeltapush.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeltaDiffReporterTest {

    private DeltaDiffReporter reporter;

    @BeforeEach
    void setUp() {
        reporter = new DeltaDiffReporter();
    }

    @Test
    void buildAndRecord_successfulDiff_noWarnings() {
        DeltaDiffReport report = reporter.buildAndRecord(
                "old.apk", "new.apk",
                10_000_000L, 10_500_000L, 500_000L,
                DeltaDiffStrategy.BSDIFF, 1500L, true);

        assertTrue(report.isSuccess());
        assertFalse(report.hasWarnings());
        assertEquals(1, reporter.getHistory().size());
        assertEquals(DeltaDiffStrategy.BSDIFF, report.getStrategy());
    }

    @Test
    void buildAndRecord_largeDelta_generatesWarning() {
        // delta = 90% of target => should warn
        DeltaDiffReport report = reporter.buildAndRecord(
                "old.apk", "new.apk",
                5_000_000L, 10_000_000L, 9_000_000L,
                DeltaDiffStrategy.BSDIFF, 2000L, true);

        assertTrue(report.hasWarnings());
        assertTrue(report.getWarnings().get(0).contains("exceeds threshold"));
    }

    @Test
    void buildAndRecord_slowDiff_generatesWarning() {
        DeltaDiffReport report = reporter.buildAndRecord(
                "old.apk", "new.apk",
                10_000_000L, 10_500_000L, 200_000L,
                DeltaDiffStrategy.BSDIFF, 15_000L, true);

        assertTrue(report.hasWarnings());
        assertTrue(report.getWarnings().stream().anyMatch(w -> w.contains("15000 ms")));
    }

    @Test
    void buildAndRecord_failureStatus_generatesWarning() {
        DeltaDiffReport report = reporter.buildAndRecord(
                "old.apk", "new.apk",
                10_000_000L, 10_500_000L, 0L,
                DeltaDiffStrategy.BSDIFF, 100L, false);

        assertFalse(report.isSuccess());
        assertTrue(report.getWarnings().stream().anyMatch(w -> w.contains("failure status")));
    }

    @Test
    void buildAndRecord_multipleReports_allStoredInHistory() {
        reporter.buildAndRecord("a.apk", "b.apk", 1000L, 1100L, 50L, DeltaDiffStrategy.BSDIFF, 100L, true);
        reporter.buildAndRecord("b.apk", "c.apk", 1100L, 1200L, 60L, DeltaDiffStrategy.BSDIFF, 120L, true);

        assertEquals(2, reporter.getHistory().size());
    }

    @Test
    void clearHistory_removesAllRecords() {
        reporter.buildAndRecord("a.apk", "b.apk", 1000L, 1100L, 50L, DeltaDiffStrategy.BSDIFF, 100L, true);
        reporter.clearHistory();

        assertTrue(reporter.getHistory().isEmpty());
    }

    @Test
    void formatSummary_containsKeyFields() {
        DeltaDiffReport report = reporter.buildAndRecord(
                "old.apk", "new.apk",
                8_000_000L, 9_000_000L, 400_000L,
                DeltaDiffStrategy.BSDIFF, 800L, true);

        String summary = reporter.formatSummary(report);
        assertTrue(summary.contains("old.apk"));
        assertTrue(summary.contains("new.apk"));
        assertTrue(summary.contains("BSDIFF"));
        assertTrue(summary.contains("SUCCESS"));
        assertTrue(summary.contains("800 ms"));
    }

    @Test
    void compressionRatio_calculatedCorrectly() {
        DeltaDiffReport report = reporter.buildAndRecord(
                "old.apk", "new.apk",
                10_000_000L, 10_000_000L, 2_000_000L,
                DeltaDiffStrategy.BSDIFF, 500L, true);

        assertEquals(0.80, report.getCompressionRatio(), 0.001);
    }

    @Test
    void buildAndRecord_nullSourcePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                reporter.buildAndRecord(null, "new.apk", 0L, 0L, 0L, DeltaDiffStrategy.BSDIFF, 0L, true));
    }
}
