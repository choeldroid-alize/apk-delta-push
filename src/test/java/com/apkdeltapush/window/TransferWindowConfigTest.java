package com.apkdeltapush.window;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class TransferWindowConfigTest {

    @Test
    void defaultValuesAreApplied() {
        TransferWindowConfig cfg = TransferWindowConfig.builder().build();
        assertEquals(16, cfg.getMaxWindowSize());
        assertEquals(4, cfg.getInitialWindowSize());
        assertEquals(Duration.ofSeconds(5), cfg.getAckTimeout());
        assertTrue(cfg.isAdaptiveScaling());
        assertEquals(2, cfg.getScaleStepSize());
    }

    @Test
    void customValuesAreRetained() {
        TransferWindowConfig cfg = TransferWindowConfig.builder()
            .maxWindowSize(32)
            .initialWindowSize(8)
            .ackTimeout(Duration.ofSeconds(10))
            .adaptiveScaling(false)
            .scaleStepSize(4)
            .build();

        assertEquals(32, cfg.getMaxWindowSize());
        assertEquals(8, cfg.getInitialWindowSize());
        assertEquals(Duration.ofSeconds(10), cfg.getAckTimeout());
        assertFalse(cfg.isAdaptiveScaling());
        assertEquals(4, cfg.getScaleStepSize());
    }

    @Test
    void throwsWhenInitialExceedsMax() {
        assertThrows(IllegalArgumentException.class, () ->
            TransferWindowConfig.builder()
                .maxWindowSize(4)
                .initialWindowSize(8)
                .build()
        );
    }

    @Test
    void equalInitialAndMaxIsAllowed() {
        assertDoesNotThrow(() ->
            TransferWindowConfig.builder()
                .maxWindowSize(8)
                .initialWindowSize(8)
                .build()
        );
    }

    @Test
    void toStringContainsKeyFields() {
        TransferWindowConfig cfg = TransferWindowConfig.builder()
            .maxWindowSize(10)
            .initialWindowSize(2)
            .build();
        String s = cfg.toString();
        assertTrue(s.contains("max=10"));
        assertTrue(s.contains("initial=2"));
    }
}
