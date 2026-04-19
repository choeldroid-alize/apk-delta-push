package com.apkdeltapush.verify;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VerificationResultTest {

    @Test
    void successResultHasCorrectState() {
        VerificationResult result = VerificationResult.success(42);
        assertTrue(result.isSuccess());
        assertEquals(42, result.getInstalledVersionCode());
        assertNull(result.getErrorMessage());
    }

    @Test
    void failureResultHasCorrectState() {
        VerificationResult result = VerificationResult.failure("something went wrong");
        assertFalse(result.isSuccess());
        assertEquals(-1, result.getInstalledVersionCode());
        assertEquals("something went wrong", result.getErrorMessage());
    }

    @Test
    void successToStringContainsVersionCode() {
        String str = VerificationResult.success(7).toString();
        assertTrue(str.contains("7"));
        assertTrue(str.contains("true"));
    }

    @Test
    void failureToStringContainsError() {
        String str = VerificationResult.failure("timeout").toString();
        assertTrue(str.contains("timeout"));
        assertTrue(str.contains("false"));
    }
}
