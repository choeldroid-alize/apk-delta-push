package com.apkdeltapush.signature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ApkSignatureValidatorTest {

    private ApkSignatureValidator validator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        validator = new ApkSignatureValidator();
    }

    @Test
    void validate_missingFile_returnsFailure() throws IOException {
        Path missing = tempDir.resolve("nonexistent.apk");
        SignatureValidationResult result = validator.validate(missing);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("not found"));
    }

    @Test
    void validate_invalidApk_returnsFailure() throws IOException {
        Path fakeApk = tempDir.resolve("fake.apk");
        Files.write(fakeApk, "not a real apk".getBytes());
        SignatureValidationResult result = validator.validate(fakeApk);
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void signatureValidationResult_success_hasFingerprint() {
        SignatureValidationResult result = SignatureValidationResult.success("abc123");
        assertTrue(result.isValid());
        assertEquals("abc123", result.getCertificateFingerprint());
        assertNull(result.getErrorMessage());
    }

    @Test
    void signatureValidationResult_failure_hasMessage() {
        SignatureValidationResult result = SignatureValidationResult.failure("bad sig");
        assertFalse(result.isValid());
        assertEquals("bad sig", result.getErrorMessage());
        assertNull(result.getCertificateFingerprint());
    }

    @Test
    void haveSameSigner_bothInvalid_returnsFalse() throws IOException {
        Path a = tempDir.resolve("a.apk");
        Path b = tempDir.resolve("b.apk");
        Files.write(a, "fake".getBytes());
        Files.write(b, "fake".getBytes());
        assertFalse(validator.haveSameSigner(a, b));
    }
}
