package com.apkdeltapush.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeltaDiffProfilerTest {

    private DeltaDiffProfiler profiler;

    @BeforeEach
    void setUp() {
        profiler = new DeltaDiffProfiler();
    }

    @Test
    void startAndEndPhase_recordsEntry() {
        profiler.startPhase("bsdiff");
        profiler.endPhase("bsdiff", 1000L, 400L);

        Map<String, DeltaDiffProfileEntry> entries = profiler.getEntries();
        assertTrue(entries.containsKey("bsdiff"));
        DeltaDiffProfileEntry entry = entries.get("bsdiff");
        assertEquals("bsdiff", entry.getPhase());
        assertEquals(1000L, entry.getInputBytes());
        assertEquals(400L, entry.getOutputBytes());
        assertFalse(entry.getElapsed().isNegative());
    }

    @Test
    void endPhase_withoutStart_throwsIllegalState() {
        assertThrows(IllegalStateException.class,
                () -> profiler.endPhase("compress", 500L, 200L));
    }

    @Test
    void startPhase_withBlankName_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> profiler.startPhase("  "));
    }

    @Test
    void getTotalElapsed_sumsAllPhases() throws InterruptedException {
        profiler.startPhase("phase1");
        Thread.sleep(10);
        profiler.endPhase("phase1", 100L, 80L);

        profiler.startPhase("phase2");
        Thread.sleep(10);
        profiler.endPhase("phase2", 200L, 150L);

        Duration total = profiler.getTotalElapsed();
        assertTrue(total.toMillis() >= 20,
                "Expected total elapsed >= 20ms but was " + total.toMillis());
    }

    @Test
    void getOverallCompressionRatio_calculatesCorrectly() {
        profiler.startPhase("a");
        profiler.endPhase("a", 1000L, 500L);
        profiler.startPhase("b");
        profiler.endPhase("b", 2000L, 1000L);

        double ratio = profiler.getOverallCompressionRatio();
        assertEquals(0.5, ratio, 0.001);
    }

    @Test
    void getOverallCompressionRatio_withNoInput_returnsOne() {
        profiler.startPhase("empty");
        profiler.endPhase("empty", 0L, 0L);

        assertEquals(1.0, profiler.getOverallCompressionRatio(), 0.001);
    }

    @Test
    void reset_clearsAllState() {
        profiler.startPhase("x");
        profiler.endPhase("x", 10L, 5L);
        profiler.reset();

        assertTrue(profiler.getEntries().isEmpty());
        assertEquals(Duration.ZERO, profiler.getTotalElapsed());
    }

    @Test
    void getEntries_returnsImmutableMap() {
        profiler.startPhase("z");
        profiler.endPhase("z", 50L, 25L);

        Map<String, DeltaDiffProfileEntry> entries = profiler.getEntries();
        assertThrows(UnsupportedOperationException.class,
                () -> entries.put("extra", null));
    }
}
