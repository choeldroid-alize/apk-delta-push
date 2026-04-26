package com.apkdeltapush.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeltaDiffFilterTest {

    private DeltaDiffFilter filter;

    @BeforeEach
    void setUp() {
        filter = new DeltaDiffFilter();
    }

    private DeltaDiffResult mockResult(String path, long deltaSize, long originalSize) {
        DeltaDiffResult r = mock(DeltaDiffResult.class);
        when(r.getFilePath()).thenReturn(path);
        when(r.getDeltaSize()).thenReturn(deltaSize);
        when(r.getOriginalSize()).thenReturn(originalSize);
        return r;
    }

    @Test
    void accepts_nullResult_returnsFalse() {
        assertFalse(filter.accepts(null));
    }

    @Test
    void accepts_noFilters_alwaysTrue() {
        DeltaDiffResult r = mockResult("classes.dex", 500L, 10000L);
        assertTrue(filter.accepts(r));
    }

    @Test
    void accepts_deltaAboveMinSize_passes() {
        filter.withMinDeltaSize(100L);
        DeltaDiffResult r = mockResult("lib.so", 200L, 5000L);
        assertTrue(filter.accepts(r));
    }

    @Test
    void accepts_deltaBelowMinSize_rejected() {
        filter.withMinDeltaSize(1000L);
        DeltaDiffResult r = mockResult("lib.so", 50L, 5000L);
        assertFalse(filter.accepts(r));
    }

    @Test
    void accepts_changeRatioWithinLimit_passes() {
        filter.withMaxChangeRatio(0.5);
        DeltaDiffResult r = mockResult("res/layout.xml", 400L, 1000L);
        assertTrue(filter.accepts(r));
    }

    @Test
    void accepts_changeRatioExceedsLimit_rejected() {
        filter.withMaxChangeRatio(0.3);
        DeltaDiffResult r = mockResult("res/layout.xml", 400L, 1000L);
        assertFalse(filter.accepts(r));
    }

    @Test
    void accepts_excludedExtension_rejected() {
        filter.withExcludedExtensions(Arrays.asList(".png", ".jpg"));
        DeltaDiffResult r = mockResult("res/icon.png", 100L, 200L);
        assertFalse(filter.accepts(r));
    }

    @Test
    void accepts_nonExcludedExtension_passes() {
        filter.withExcludedExtensions(Arrays.asList(".png"));
        DeltaDiffResult r = mockResult("classes.dex", 100L, 200L);
        assertTrue(filter.accepts(r));
    }

    @Test
    void accepts_customPredicateRejects_returnsFalse() {
        filter.withCustomPredicate(r -> !r.getFilePath().contains("debug"));
        DeltaDiffResult r = mockResult("debug/classes.dex", 100L, 500L);
        assertFalse(filter.accepts(r));
    }

    @Test
    void filter_returnsOnlyAcceptedResults() {
        filter.withMinDeltaSize(100L);
        DeltaDiffResult r1 = mockResult("a.dex", 200L, 1000L);
        DeltaDiffResult r2 = mockResult("b.dex", 50L, 1000L);
        DeltaDiffResult r3 = mockResult("c.dex", 150L, 1000L);

        List<DeltaDiffResult> result = filter.filter(Arrays.asList(r1, r2, r3));

        assertEquals(2, result.size());
        assertTrue(result.contains(r1));
        assertTrue(result.contains(r3));
    }

    @Test
    void filter_nullList_returnsEmptyList() {
        List<DeltaDiffResult> result = filter.filter(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void withMinDeltaSize_negativeValue_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> filter.withMinDeltaSize(-1L));
    }

    @Test
    void withMaxChangeRatio_outOfRange_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> filter.withMaxChangeRatio(1.5));
        assertThrows(IllegalArgumentException.class, () -> filter.withMaxChangeRatio(-0.1));
    }
}
