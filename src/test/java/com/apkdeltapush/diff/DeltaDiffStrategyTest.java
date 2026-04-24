package com.apkdeltapush.diff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeltaDiffStrategyTest {

    @Test
    void supportsStreaming_xorReturnsTrue() {
        assertTrue(DeltaDiffStrategy.XOR.supportsStreaming());
    }

    @Test
    void supportsStreaming_zipEntryReturnsTrue() {
        assertTrue(DeltaDiffStrategy.ZIP_ENTRY.supportsStreaming());
    }

    @Test
    void supportsStreaming_bsdiffReturnsFalse() {
        assertFalse(DeltaDiffStrategy.BSDIFF.supportsStreaming());
    }

    @Test
    void supportsStreaming_autoReturnsFalse() {
        assertFalse(DeltaDiffStrategy.AUTO.supportsStreaming());
    }

    @Test
    void recommended_smallApkUsesBsdiff() {
        long size = 2 * 1024 * 1024; // 2 MB
        assertEquals(DeltaDiffStrategy.BSDIFF, DeltaDiffStrategy.recommended(size));
    }

    @Test
    void recommended_mediumApkUsesZipEntry() {
        long size = 20 * 1024 * 1024; // 20 MB
        assertEquals(DeltaDiffStrategy.ZIP_ENTRY, DeltaDiffStrategy.recommended(size));
    }

    @Test
    void recommended_largeApkUsesXor() {
        long size = 100 * 1024 * 1024; // 100 MB
        assertEquals(DeltaDiffStrategy.XOR, DeltaDiffStrategy.recommended(size));
    }

    @Test
    void recommended_boundaryExactly5MbUsesBsdiff() {
        long size = 5 * 1024 * 1024;
        assertEquals(DeltaDiffStrategy.BSDIFF, DeltaDiffStrategy.recommended(size));
    }

    @Test
    void allStrategiesPresent() {
        assertEquals(4, DeltaDiffStrategy.values().length);
    }
}
