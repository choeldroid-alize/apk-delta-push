package com.apkdeltapush.verify;

import com.apkdeltapush.adb.AdbClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstallVerifierTest {

    private AdbClient adbClient;
    private InstallVerifier verifier;

    @BeforeEach
    void setUp() {
        adbClient = Mockito.mock(AdbClient.class);
        verifier = new InstallVerifier(adbClient);
    }

    @Test
    void constructorRejectsNullAdbClient() {
        assertThrows(IllegalArgumentException.class, () -> new InstallVerifier(null));
    }

    @Test
    void verifyReturnsSuccessWhenVersionMatches() throws Exception {
        when(adbClient.runShellCommand(anyString(), anyString()))
            .thenReturn("    versionCode=10 minSdk=21 targetSdk=33");

        VerificationResult result = verifier.verify("emulator-5554", "com.example.app", 10);

        assertTrue(result.isSuccess());
        assertEquals(10, result.getInstalledVersionCode());
    }

    @Test
    void verifyReturnsSuccessWhenInstalledVersionIsHigher() throws Exception {
        when(adbClient.runShellCommand(anyString(), anyString()))
            .thenReturn("    versionCode=15 minSdk=21 targetSdk=33");

        VerificationResult result = verifier.verify("emulator-5554", "com.example.app", 10);

        assertTrue(result.isSuccess());
        assertEquals(15, result.getInstalledVersionCode());
    }

    @Test
    void verifyReturnsFailureWhenVersionTooLow() throws Exception {
        when(adbClient.runShellCommand(anyString(), anyString()))
            .thenReturn("    versionCode=5 minSdk=21 targetSdk=33");

        VerificationResult result = verifier.verify("emulator-5554", "com.example.app", 10);

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void verifyReturnsFailureWhenPackageNotFound() throws Exception {
        when(adbClient.runShellCommand(anyString(), anyString())).thenReturn("");

        VerificationResult result = verifier.verify("emulator-5554", "com.missing.app", 1);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("not found"));
    }

    @Test
    void verifyReturnsFailureOnNullArgs() {
        VerificationResult result = verifier.verify(null, "com.example.app", 1);
        assertFalse(result.isSuccess());
    }

    @Test
    void verifyHandlesAdbException() throws Exception {
        when(adbClient.runShellCommand(anyString(), anyString()))
            .thenThrow(new RuntimeException("ADB disconnected"));

        VerificationResult result = verifier.verify("emulator-5554", "com.example.app", 1);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("ADB error"));
    }
}
