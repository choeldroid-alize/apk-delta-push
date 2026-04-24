package com.apkdeltapush.validation;

import com.apkdeltapush.adb.AdbClient;
import com.apkdeltapush.signature.ApkSignatureValidator;
import com.apkdeltapush.util.ApkVersionChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PushPreflightValidatorTest {

    private AdbClient adbClient;
    private ApkSignatureValidator signatureValidator;
    private ApkVersionChecker versionChecker;
    private PushPreflightValidator validator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        adbClient = mock(AdbClient.class);
        signatureValidator = mock(ApkSignatureValidator.class);
        versionChecker = mock(ApkVersionChecker.class);
        validator = new PushPreflightValidator(adbClient, signatureValidator, versionChecker);
    }

    @Test
    void validate_allChecksPass_returnsPassedResult() throws IOException {
        File apk = tempDir.resolve("app.apk").toFile();
        apk.createNewFile();
        when(adbClient.isDeviceConnected("emulator-5554")).thenReturn(true);
        when(signatureValidator.validate(apk)).thenReturn(true);
        when(versionChecker.isNewerThanInstalled("emulator-5554", apk)).thenReturn(true);

        PreflightResult result = validator.validate("emulator-5554", apk);

        assertTrue(result.isPassed());
        assertTrue(result.getViolations().isEmpty());
        assertTrue(result.getSummary().contains("passed"));
    }

    @Test
    void validate_deviceNotConnected_addsViolation() throws IOException {
        File apk = tempDir.resolve("app.apk").toFile();
        apk.createNewFile();
        when(adbClient.isDeviceConnected("emulator-5554")).thenReturn(false);

        PreflightResult result = validator.validate("emulator-5554", apk);

        assertFalse(result.isPassed());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("not connected")));
    }

    @Test
    void validate_apkFileNull_addsViolation() {
        when(adbClient.isDeviceConnected("emulator-5554")).thenReturn(true);

        PreflightResult result = validator.validate("emulator-5554", null);

        assertFalse(result.isPassed());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("does not exist")));
    }

    @Test
    void validate_signatureFails_addsViolation() throws IOException {
        File apk = tempDir.resolve("app.apk").toFile();
        apk.createNewFile();
        when(adbClient.isDeviceConnected("emulator-5554")).thenReturn(true);
        when(signatureValidator.validate(apk)).thenReturn(false);

        PreflightResult result = validator.validate("emulator-5554", apk);

        assertFalse(result.isPassed());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("signature")));
    }

    @Test
    void validate_versionNotNewer_addsViolation() throws IOException {
        File apk = tempDir.resolve("app.apk").toFile();
        apk.createNewFile();
        when(adbClient.isDeviceConnected("emulator-5554")).thenReturn(true);
        when(signatureValidator.validate(apk)).thenReturn(true);
        when(versionChecker.isNewerThanInstalled("emulator-5554", apk)).thenReturn(false);

        PreflightResult result = validator.validate("emulator-5554", apk);

        assertFalse(result.isPassed());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("not newer")));
    }

    @Test
    void validate_nullDeviceSerial_addsViolation() throws IOException {
        File apk = tempDir.resolve("app.apk").toFile();
        apk.createNewFile();

        PreflightResult result = validator.validate(null, apk);

        assertFalse(result.isPassed());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("serial")));
    }
}
