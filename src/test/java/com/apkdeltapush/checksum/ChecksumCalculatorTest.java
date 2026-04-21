package com.apkdeltapush.checksum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class ChecksumCalculatorTest {

    private ChecksumCalculator calculator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        calculator = new ChecksumCalculator();
    }

    @Test
    void computeSha256_knownContent_returnsExpectedHash() throws Exception {
        Path file = tempDir.resolve("test.apk");
        Files.writeString(file, "hello", StandardCharsets.UTF_8);
        // SHA-256 of "hello" is well-known
        String expected = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";
        String actual = calculator.compute(file, ChecksumCalculator.Algorithm.SHA256);
        assertEquals(expected, actual);
    }

    @Test
    void computeMd5_knownContent_returnsExpectedHash() throws Exception {
        Path file = tempDir.resolve("test.bin");
        Files.writeString(file, "hello", StandardCharsets.UTF_8);
        // MD5 of "hello"
        String expected = "5d41402abc4b2a76b9719d911017c592";
        String actual = calculator.compute(file, ChecksumCalculator.Algorithm.MD5);
        assertEquals(expected, actual);
    }

    @Test
    void computeSha256_byteArray_returnsCorrectHash() throws NoSuchAlgorithmException {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        String expected = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";
        String actual = calculator.computeSha256(data);
        assertEquals(expected, actual);
    }

    @Test
    void verify_correctChecksum_returnsTrue() throws Exception {
        Path file = tempDir.resolve("delta.patch");
        Files.writeString(file, "delta-content", StandardCharsets.UTF_8);
        String checksum = calculator.compute(file, ChecksumCalculator.Algorithm.SHA256);
        assertTrue(calculator.verify(file, ChecksumCalculator.Algorithm.SHA256, checksum));
    }

    @Test
    void verify_incorrectChecksum_returnsFalse() throws Exception {
        Path file = tempDir.resolve("delta.patch");
        Files.writeString(file, "delta-content", StandardCharsets.UTF_8);
        assertFalse(calculator.verify(file, ChecksumCalculator.Algorithm.SHA256, "deadbeef"));
    }

    @Test
    void verify_caseInsensitive_returnsTrue() throws Exception {
        Path file = tempDir.resolve("apk.patch");
        Files.writeString(file, "hello", StandardCharsets.UTF_8);
        String upper = "2CF24DBA5FB0A30E26E83B2AC5B9E29E1B161E5C1FA7425E73043362938B9824";
        assertTrue(calculator.verify(file, ChecksumCalculator.Algorithm.SHA256, upper));
    }

    @Test
    void compute_emptyFile_returnsNonEmptyHash() throws Exception {
        Path file = tempDir.resolve("empty.apk");
        Files.createFile(file);
        String hash = calculator.compute(file, ChecksumCalculator.Algorithm.SHA1);
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void compute_nonExistentFile_throwsIOException() {
        Path missing = tempDir.resolve("missing.apk");
        assertThrows(IOException.class,
                () -> calculator.compute(missing, ChecksumCalculator.Algorithm.SHA256));
    }
}
