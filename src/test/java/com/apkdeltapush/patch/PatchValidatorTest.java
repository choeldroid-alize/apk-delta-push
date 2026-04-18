package com.apkdeltapush.patch;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class PatchValidatorTest {

    private PatchValidator validator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        validator = new PatchValidator();
    }

    @Test
    void computeChecksum_returnsConsistentHash() throws Exception {
        Path file = tempDir.resolve("data.bin");
        Files.write(file, "hello patch".getBytes(StandardCharsets.UTF_8));
        String first  = validator.computeChecksum(file);
        String second = validator.computeChecksum(file);
        assertEquals(first, second);
        assertEquals(64, first.length(), "SHA-256 hex should be 64 chars");
    }

    @Test
    void validate_returnsTrueForCorrectChecksum() throws Exception {
        Path file = tempDir.resolve("patch.bin");
        Files.write(file, "delta-data".getBytes(StandardCharsets.UTF_8));
        String checksum = validator.computeChecksum(file);
        assertTrue(validator.validate(file, checksum));
    }

    @Test
    void validate_returnsFalseForWrongChecksum() throws Exception {
        Path file = tempDir.resolve("patch.bin");
        Files.write(file, "delta-data".getBytes(StandardCharsets.UTF_8));
        assertFalse(validator.validate(file, "0000000000000000000000000000000000000000000000000000000000000000"));
    }

    @Test
    void validate_returnsFalseForMissingFile() {
        Path missing = tempDir.resolve("ghost.bin");
        assertFalse(validator.validate(missing, "abc123"));
    }
}
