package com.apkdeltapush.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeltaDiffValidatorTest {

    private DeltaDiffValidator validator;
    private DeltaDiffOptions defaultOptions;

    @BeforeEach
    void setUp() {
        validator = new DeltaDiffValidator();
        defaultOptions = mock(DeltaDiffOptions.class);
        when(defaultOptions.isRequireMetadata()).thenReturn(false);
    }

    private DeltaDiffResult buildResult(long sourceSize, long targetSize, long deltaSize,
                                        String checksum, Object metadata) {
        DeltaDiffResult r = mock(DeltaDiffResult.class);
        when(r.getSourceSize()).thenReturn(sourceSize);
        when(r.getTargetSize()).thenReturn(targetSize);
        when(r.getDeltaSize()).thenReturn(deltaSize);
        when(r.getChecksum()).thenReturn(checksum);
        when(r.getMetadata()).thenReturn(metadata);
        return r;
    }

    @Test
    void validResult_producesValidOutcome() {
        DeltaDiffResult result = buildResult(1_000_000L, 1_050_000L, 800_000L, "abc123", null);
        var outcome = validator.validate(result, defaultOptions);
        assertTrue(outcome.isValid());
        assertTrue(outcome.getViolations().isEmpty());
    }

    @Test
    void zeroSourceSize_isInvalid() {
        DeltaDiffResult result = buildResult(0L, 500_000L, 400_000L, "abc123", null);
        var outcome = validator.validate(result, defaultOptions);
        assertFalse(outcome.isValid());
        assertTrue(outcome.getViolations().stream().anyMatch(v -> v.contains("sourceSize")));
    }

    @Test
    void negativeDeltaSize_isInvalid() {
        DeltaDiffResult result = buildResult(500_000L, 500_000L, -1L, "abc123", null);
        var outcome = validator.validate(result, defaultOptions);
        assertFalse(outcome.isValid());
        assertTrue(outcome.getViolations().stream().anyMatch(v -> v.contains("deltaSize")));
    }

    @Test
    void deltaExceedsSourceByMoreThan5Percent_isInvalid() {
        // delta = 1.10 * source -> ratio 1.10 > 1.05
        DeltaDiffResult result = buildResult(1_000_000L, 1_100_000L, 1_100_000L, "abc123", null);
        var outcome = validator.validate(result, defaultOptions);
        assertFalse(outcome.isValid());
        assertTrue(outcome.getViolations().stream().anyMatch(v -> v.contains("ratio")));
    }

    @Test
    void blankChecksum_isInvalid() {
        DeltaDiffResult result = buildResult(1_000_000L, 1_000_000L, 900_000L, "  ", null);
        var outcome = validator.validate(result, defaultOptions);
        assertFalse(outcome.isValid());
        assertTrue(outcome.getViolations().stream().anyMatch(v -> v.contains("checksum")));
    }

    @Test
    void metadataRequiredButNull_isInvalid() {
        when(defaultOptions.isRequireMetadata()).thenReturn(true);
        DeltaDiffResult result = buildResult(1_000_000L, 1_000_000L, 900_000L, "abc123", null);
        var outcome = validator.validate(result, defaultOptions);
        assertFalse(outcome.isValid());
        assertTrue(outcome.getViolations().stream().anyMatch(v -> v.contains("metadata")));
    }

    @Test
    void metadataRequiredAndPresent_isValid() {
        when(defaultOptions.isRequireMetadata()).thenReturn(true);
        DeltaDiffResult result = buildResult(1_000_000L, 1_000_000L, 900_000L, "abc123", new Object());
        var outcome = validator.validate(result, defaultOptions);
        assertTrue(outcome.isValid());
    }

    @Test
    void nullResult_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> validator.validate(null, defaultOptions));
    }

    @Test
    void nullOptions_throwsNullPointerException() {
        DeltaDiffResult result = buildResult(1_000_000L, 1_000_000L, 900_000L, "abc123", null);
        assertThrows(NullPointerException.class, () -> validator.validate(result, null));
    }
}
