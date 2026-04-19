package com.apkdeltapush.checksum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ChecksumVerifierTest {

    private final ChecksumVerifier verifier = new ChecksumVerifier();

    @TempDir
    Path tempDir;

    @Test
    void computeReturnsSha256HexString() throws Exception {
        Path file = tempDir.resolve("test.apk");
        Files.write(file, "hello world".getBytes());
        String checksum = verifier.compute(file);
        assertNotNull(checksum);
        assertEquals(64, checksum.length());
        assertTrue(checksum.matches("[0-9a-f]+"));
    }

    @Test
    void computeIsDeterministic() throws Exception {
        Path file = tempDir.resolve("apk.bin");
        Files.write(file, "apk-content".getBytes());
        assertEquals(verifier.compute(file), verifier.compute(file));
    }

    @Test
    void verifyReturnsTrueForCorrectChecksum() throws Exception {
        Path file = tempDir.resolve("patch.bin");
        Files.write(file, "patch-data".getBytes());
        String expected = verifier.compute(file);
        ChecksumResult result = verifier.verify(file, expected);
        assertTrue(result.isValid());
        assertEquals(expected, result.getExpected());
        assertEquals(expected, result.getActual());
    }

    @Test
    void verifyReturnsFalseForWrongChecksum() throws Exception {
        Path file = tempDir.resolve("patch2.bin");
        Files.write(file, "patch-data".getBytes());
        ChecksumResult result = verifier.verify(file, "deadbeef");
        assertFalse(result.isValid());
        assertEquals("deadbeef", result.getExpected());
        assertNotEquals("deadbeef", result.getActual());
    }

    @Test
    void checksumDiffersForDifferentContent() throws Exception {
        Path file1 = tempDir.resolve("a.apk");
        Path file2 = tempDir.resolve("b.apk");
        Files.write(file1, "content-a".getBytes());
        Files.write(file2, "content-b".getBytes());
        assertNotEquals(verifier.compute(file1), verifier.compute(file2));
    }
}
