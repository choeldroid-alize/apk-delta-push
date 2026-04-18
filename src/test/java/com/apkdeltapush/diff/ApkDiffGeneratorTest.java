package com.apkdeltapush.diff;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class ApkDiffGeneratorTest {

    private ApkDiffGenerator generator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        generator = new ApkDiffGenerator();
    }

    @Test
    void testDiffProducesNonEmptyPatch() throws IOException {
        Path oldApk = tempDir.resolve("old.apk");
        Path newApk = tempDir.resolve("new.apk");
        Files.write(oldApk, new byte[]{0x50, 0x4B, 0x03, 0x04, 0x01, 0x02});
        Files.write(newApk, new byte[]{0x50, 0x4B, 0x03, 0x04, 0x01, 0x03});

        DiffResult result = generator.generate(oldApk, newApk);

        assertNotNull(result);
        assertNotNull(result.getPatchBytes());
        assertTrue(result.getPatchBytes().length > 0);
    }

    @Test
    void testDiffChecksumsDiffer() throws IOException {
        Path oldApk = tempDir.resolve("old.apk");
        Path newApk = tempDir.resolve("new.apk");
        Files.write(oldApk, "old content".getBytes());
        Files.write(newApk, "new content".getBytes());

        DiffResult result = generator.generate(oldApk, newApk);

        assertNotEquals(result.getOldChecksum(), result.getNewChecksum());
    }

    @Test
    void testDiffIdenticalFilesProducesZeroPatch() throws IOException {
        byte[] content = "identical apk content".getBytes();
        Path oldApk = tempDir.resolve("old.apk");
        Path newApk = tempDir.resolve("new.apk");
        Files.write(oldApk, content);
        Files.write(newApk, content);

        DiffResult result = generator.generate(oldApk, newApk);

        assertEquals(result.getOldChecksum(), result.getNewChecksum());
        // XOR of identical bytes should be all zeros (after 4-byte header)
        byte[] patch = result.getPatchBytes();
        for (int i = 4; i < patch.length; i++) {
            assertEquals(0, patch[i], "Expected zero XOR at index " + i);
        }
    }

    @Test
    void testSizesRecordedCorrectly() throws IOException {
        byte[] oldContent = new byte[100];
        byte[] newContent = new byte[150];
        Path oldApk = tempDir.resolve("old.apk");
        Path newApk = tempDir.resolve("new.apk");
        Files.write(oldApk, oldContent);
        Files.write(newApk, newContent);

        DiffResult result = generator.generate(oldApk, newApk);

        assertEquals(100, result.getOldSizeBytes());
        assertEquals(150, result.getNewSizeBytes());
    }
}
