package com.apkdeltapush.stats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PushStatsCollectorTest {

    private PushStatsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new PushStatsCollector();
    }

    @Test
    void initialStateIsZero() {
        assertEquals(0, collector.getTotalPushAttempts());
        assertEquals(0, collector.getSuccessfulPushes());
        assertEquals(0, collector.getFailedPushes());
        assertEquals(0, collector.getTotalBytesTransferred());
    }

    @Test
    void recordPushAttemptIncrementsCounter() {
        collector.recordPushAttempt();
        collector.recordPushAttempt();
        assertEquals(2, collector.getTotalPushAttempts());
    }

    @Test
    void recordSuccessAccumulatesBytesAndDuration() {
        collector.recordPushAttempt();
        collector.recordSuccess(1024L, 200L);
        collector.recordPushAttempt();
        collector.recordSuccess(2048L, 400L);

        assertEquals(2, collector.getSuccessfulPushes());
        assertEquals(3072L, collector.getTotalBytesTransferred());
    }

    @Test
    void recordFailureIncrementsFailureCounter() {
        collector.recordPushAttempt();
        collector.recordFailure();
        assertEquals(1, collector.getFailedPushes());
        assertEquals(0, collector.getSuccessfulPushes());
    }

    @Test
    void buildSummaryComputesSuccessRate() {
        collector.recordPushAttempt();
        collector.recordSuccess(512L, 100L);
        collector.recordPushAttempt();
        collector.recordFailure();

        PushStatsSummary summary = collector.buildSummary();
        assertEquals(2, summary.getTotalAttempts());
        assertEquals(1, summary.getSuccessfulPushes());
        assertEquals(1, summary.getFailedPushes());
        assertEquals(50.0, summary.getSuccessRatePercent(), 0.001);
        assertEquals(100.0, summary.getAvgDurationMs(), 0.001);
        assertNotNull(summary.getCollectedAt());
    }

    @Test
    void buildSummaryWithNoAttemptsReturnsZeroRate() {
        PushStatsSummary summary = collector.buildSummary();
        assertEquals(0.0, summary.getSuccessRatePercent(), 0.001);
        assertEquals(0.0, summary.getAvgDurationMs(), 0.001);
    }

    @Test
    void resetClearsAllCounters() {
        collector.recordPushAttempt();
        collector.recordSuccess(1024L, 300L);
        collector.reset();

        assertEquals(0, collector.getTotalPushAttempts());
        assertEquals(0, collector.getSuccessfulPushes());
        assertEquals(0, collector.getTotalBytesTransferred());
    }

    @Test
    void recordSuccessThrowsOnNegativeBytes() {
        assertThrows(IllegalArgumentException.class, () -> collector.recordSuccess(-1L, 100L));
    }

    @Test
    void recordSuccessThrowsOnNegativeDuration() {
        assertThrows(IllegalArgumentException.class, () -> collector.recordSuccess(100L, -1L));
    }

    @Test
    void summaryToStringContainsKeyFields() {
        collector.recordPushAttempt();
        collector.recordSuccess(256L, 50L);
        String str = collector.buildSummary().toString();
        assertTrue(str.contains("attempts=1"));
        assertTrue(str.contains("successes=1"));
        assertTrue(str.contains("bytes=256"));
    }
}
