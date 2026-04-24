package com.apkdeltapush.diff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeltaDiffOptionsTest {

    @Test
    void defaultBuilderHasSensibleDefaults() {
        DeltaDiffOptions opts = DeltaDiffOptions.builder().build();
        assertEquals(DeltaDiffStrategy.AUTO, opts.getStrategy());
        assertEquals(6, opts.getCompressionLevel());
        assertTrue(opts.isValidateOutput());
        assertEquals(0, opts.getMaxDeltaSizeBytes());
    }

    @Test
    void builderAppliesAllFields() {
        DeltaDiffOptions opts = DeltaDiffOptions.builder()
                .strategy(DeltaDiffStrategy.BSDIFF)
                .compressionLevel(9)
                .validateOutput(false)
                .maxDeltaSizeBytes(10_000_000L)
                .build();

        assertEquals(DeltaDiffStrategy.BSDIFF, opts.getStrategy());
        assertEquals(9, opts.getCompressionLevel());
        assertFalse(opts.isValidateOutput());
        assertEquals(10_000_000L, opts.getMaxDeltaSizeBytes());
    }

    @Test
    void compressionLevelBelowOneThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> DeltaDiffOptions.builder().compressionLevel(0));
    }

    @Test
    void compressionLevelAboveNineThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> DeltaDiffOptions.builder().compressionLevel(10));
    }

    @Test
    void nullStrategyThrows() {
        assertThrows(NullPointerException.class,
                () -> DeltaDiffOptions.builder().strategy(null));
    }

    @Test
    void toStringContainsStrategy() {
        DeltaDiffOptions opts = DeltaDiffOptions.builder()
                .strategy(DeltaDiffStrategy.XOR)
                .build();
        assertTrue(opts.toString().contains("XOR"));
    }

    @Test
    void buildIsImmutable() {
        DeltaDiffOptions.Builder builder = DeltaDiffOptions.builder().compressionLevel(3);
        DeltaDiffOptions first = builder.build();
        builder.compressionLevel(8);
        DeltaDiffOptions second = builder.build();
        assertEquals(3, first.getCompressionLevel());
        assertEquals(8, second.getCompressionLevel());
    }
}
