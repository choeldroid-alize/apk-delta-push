package com.apkdeltapush.diff;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeltaDiffSummaryTest {

    private DeltaDiffSummary buildSample() {
        return DeltaDiffSummary.builder()
                .sourceApkPath("/apks/old.apk")
                .targetApkPath("/apks/new.apk")
                .sourceSizeBytes(10_000_000L)
                .targetSizeBytes(12_000_000L)
                .deltaSizeBytes(3_000_000L)
                .totalEntries(200)
                .changedEntries(15)
                .addedEntries(5)
                .removedEntries(2)
                .durationMillis(420L)
                .changedEntryNames(Arrays.asList("classes.dex", "res/layout/main.xml"))
                .build();
    }

    @Test
    void fieldsArePopulatedCorrectly() {
        DeltaDiffSummary s = buildSample();
        assertEquals("/apks/old.apk", s.getSourceApkPath());
        assertEquals("/apks/new.apk", s.getTargetApkPath());
        assertEquals(10_000_000L, s.getSourceSizeBytes());
        assertEquals(12_000_000L, s.getTargetSizeBytes());
        assertEquals(3_000_000L, s.getDeltaSizeBytes());
        assertEquals(200, s.getTotalEntries());
        assertEquals(15, s.getChangedEntries());
        assertEquals(5, s.getAddedEntries());
        assertEquals(2, s.getRemovedEntries());
        assertEquals(420L, s.getDurationMillis());
    }

    @Test
    void reductionRatioIsCalculatedCorrectly() {
        DeltaDiffSummary s = buildSample();
        // delta=3_000_000, target=12_000_000 => ratio = 1 - 0.25 = 0.75
        assertEquals(0.75, s.getReductionRatio(), 1e-9);
    }

    @Test
    void reductionRatioIsZeroWhenTargetSizeIsZero() {
        DeltaDiffSummary s = DeltaDiffSummary.builder()
                .targetSizeBytes(0)
                .deltaSizeBytes(0)
                .build();
        assertEquals(0.0, s.getReductionRatio(), 1e-9);
    }

    @Test
    void changedEntryNamesListIsUnmodifiable() {
        DeltaDiffSummary s = buildSample();
        List<String> names = s.getChangedEntryNames();
        assertThrows(UnsupportedOperationException.class, () -> names.add("extra"));
    }

    @Test
    void changedEntryNamesContainsExpectedValues() {
        DeltaDiffSummary s = buildSample();
        assertTrue(s.getChangedEntryNames().contains("classes.dex"));
        assertTrue(s.getChangedEntryNames().contains("res/layout/main.xml"));
    }

    @Test
    void generatedAtDefaultsToNonNull() {
        DeltaDiffSummary s = DeltaDiffSummary.builder().build();
        assertNotNull(s.getGeneratedAt());
    }

    @Test
    void generatedAtCanBeOverridden() {
        Instant fixed = Instant.parse("2024-06-01T10:00:00Z");
        DeltaDiffSummary s = DeltaDiffSummary.builder().generatedAt(fixed).build();
        assertEquals(fixed, s.getGeneratedAt());
    }

    @Test
    void toStringContainsKeyFields() {
        DeltaDiffSummary s = buildSample();
        String str = s.toString();
        assertTrue(str.contains("old.apk"));
        assertTrue(str.contains("new.apk"));
        assertTrue(str.contains("3000000"));
    }

    @Test
    void emptyBuilderProducesDefaultValues() {
        DeltaDiffSummary s = DeltaDiffSummary.builder().build();
        assertEquals("", s.getSourceApkPath());
        assertEquals(0L, s.getDeltaSizeBytes());
        assertEquals(0, s.getChangedEntries());
        assertTrue(s.getChangedEntryNames().isEmpty());
    }
}
