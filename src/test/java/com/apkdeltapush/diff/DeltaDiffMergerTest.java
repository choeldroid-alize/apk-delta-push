package com.apkdeltapush.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeltaDiffMergerTest {

    private DeltaDiffMerger merger;

    @BeforeEach
    void setUp() {
        merger = new DeltaDiffMerger();
    }

    private DeltaDiffResult makeResult(long orig, long patched, long delta, boolean success) {
        DeltaDiffResult r = new DeltaDiffResult();
        r.setOriginalSize(orig);
        r.setPatchedSize(patched);
        r.setDeltaSize(delta);
        r.setSuccess(success);
        r.setWarnings(new ArrayList<>());
        return r;
    }

    @Test
    void merge_singleResult_returnsSameValues() {
        DeltaDiffResult r = makeResult(1000, 1100, 200, true);
        DeltaDiffResult merged = merger.merge(Collections.singletonList(r));
        assertEquals(1000, merged.getOriginalSize());
        assertEquals(1100, merged.getPatchedSize());
        assertEquals(200, merged.getDeltaSize());
        assertTrue(merged.isSuccess());
    }

    @Test
    void merge_multipleResults_sumsAllSizes() {
        List<DeltaDiffResult> results = List.of(
            makeResult(500, 600, 100, true),
            makeResult(300, 350, 80, true),
            makeResult(200, 210, 30, true)
        );
        DeltaDiffResult merged = merger.merge(results);
        assertEquals(1000, merged.getOriginalSize());
        assertEquals(1160, merged.getPatchedSize());
        assertEquals(210, merged.getDeltaSize());
        assertTrue(merged.isSuccess());
    }

    @Test
    void merge_anyFailedResult_mergedIsNotSuccess() {
        List<DeltaDiffResult> results = List.of(
            makeResult(500, 600, 100, true),
            makeResult(300, 350, 80, false)
        );
        DeltaDiffResult merged = merger.merge(results);
        assertFalse(merged.isSuccess());
    }

    @Test
    void merge_warningsAreCombined() {
        DeltaDiffResult r1 = makeResult(100, 110, 20, true);
        r1.setWarnings(List.of("warn1", "warn2"));
        DeltaDiffResult r2 = makeResult(200, 210, 30, true);
        r2.setWarnings(List.of("warn3"));
        DeltaDiffResult merged = merger.merge(List.of(r1, r2));
        assertEquals(3, merged.getWarnings().size());
        assertTrue(merged.getWarnings().contains("warn1"));
        assertTrue(merged.getWarnings().contains("warn3"));
    }

    @Test
    void merge_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> merger.merge(null));
    }

    @Test
    void merge_emptyList_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> merger.merge(Collections.emptyList()));
    }

    @Test
    void merge_exceedsMaxInputs_throwsIllegalArgumentException() {
        List<DeltaDiffResult> big = new ArrayList<>();
        for (int i = 0; i <= DeltaDiffMerger.MAX_MERGE_INPUTS; i++) {
            big.add(makeResult(10, 11, 2, true));
        }
        assertThrows(IllegalArgumentException.class, () -> merger.merge(big));
    }

    @Test
    void allSuccessful_allTrue_returnsTrue() {
        List<DeltaDiffResult> results = List.of(
            makeResult(100, 110, 20, true),
            makeResult(200, 210, 30, true)
        );
        assertTrue(merger.allSuccessful(results));
    }

    @Test
    void allSuccessful_oneFailed_returnsFalse() {
        List<DeltaDiffResult> results = List.of(
            makeResult(100, 110, 20, true),
            makeResult(200, 210, 30, false)
        );
        assertFalse(merger.allSuccessful(results));
    }

    @Test
    void allSuccessful_nullOrEmpty_returnsFalse() {
        assertFalse(merger.allSuccessful(null));
        assertFalse(merger.allSuccessful(Collections.emptyList()));
    }
}
