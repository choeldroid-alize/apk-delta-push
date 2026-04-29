package com.apkdeltapush.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeltaDiffMetricsTest {

    private DeltaDiffMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new DeltaDiffMetrics();
    }

    @Test
    void initialStateIsZero() {
        assertEquals(0, metrics.getTotalDiffsComputed());
        assertEquals(0, metrics.getTotalDiffsFailed());
        assertEquals(0, metrics.getTotalBytesIn());
        assertEquals(0, metrics.getTotalBytesOut());
        assertEquals(0, metrics.getTotalComputeTimeMs());
        assertEquals(0.0, metrics.getAverageComputeTimeMs());
        assertEquals(0.0, metrics.getCompressionRatio());
        assertNull(metrics.getLastDiffTimestamp());
    }

    @Test
    void recordDiffAccumulatesValues() {
        metrics.recordDiff(1000, 400, 50);
        metrics.recordDiff(2000, 600, 100);

        assertEquals(2, metrics.getTotalDiffsComputed());
        assertEquals(3000, metrics.getTotalBytesIn());
        assertEquals(1000, metrics.getTotalBytesOut());
        assertEquals(150, metrics.getTotalComputeTimeMs());
        assertEquals(75.0, metrics.getAverageComputeTimeMs());
        assertNotNull(metrics.getLastDiffTimestamp());
    }

    @Test
    void compressionRatioIsCorrect() {
        metrics.recordDiff(1000, 250, 10);
        assertEquals(0.25, metrics.getCompressionRatio(), 1e-9);
    }

    @Test
    void recordFailureIncrementsFailureCount() {
        metrics.recordFailure();
        metrics.recordFailure();
        assertEquals(2, metrics.getTotalDiffsFailed());
        assertEquals(0, metrics.getTotalDiffsComputed());
    }

    @Test
    void resetClearsAllCounters() {
        metrics.recordDiff(500, 200, 30);
        metrics.recordFailure();
        metrics.reset();

        assertEquals(0, metrics.getTotalDiffsComputed());
        assertEquals(0, metrics.getTotalDiffsFailed());
        assertEquals(0, metrics.getTotalBytesIn());
        assertNull(metrics.getLastDiffTimestamp());
    }

    @Test
    void negativeValuesThrowException() {
        assertThrows(IllegalArgumentException.class, () -> metrics.recordDiff(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> metrics.recordDiff(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> metrics.recordDiff(0, 0, -1));
    }

    @Test
    void toStringContainsKeyFields() {
        metrics.recordDiff(800, 200, 20);
        String s = metrics.toString();
        assertTrue(s.contains("computed=1"));
        assertTrue(s.contains("bytesIn=800"));
        assertTrue(s.contains("bytesOut=200"));
    }
}
