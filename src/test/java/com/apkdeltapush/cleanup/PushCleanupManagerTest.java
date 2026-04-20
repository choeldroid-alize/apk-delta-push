package com.apkdeltapush.cleanup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PushCleanupManagerTest {

    private PushCleanupManager cleanupManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        cleanupManager = new PushCleanupManager(true);
    }

    @Test
    void testRegisterAndCleanupSingleFile() throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "patch", ".delta");
        assertTrue(tempFile.toFile().exists());

        cleanupManager.register(tempFile);
        CleanupResult result = cleanupManager.cleanup();

        assertFalse(tempFile.toFile().exists());
        assertEquals(1, result.getDeletedCount());
        assertTrue(result.getFailedPaths().isEmpty());
    }

    @Test
    void testCleanupDirectory() throws IOException {
        Path subDir = Files.createTempDirectory(tempDir, "staging");
        Files.createTempFile(subDir, "apk", ".tmp");
        Files.createTempFile(subDir, "patch", ".bin");

        cleanupManager.register(subDir);
        CleanupResult result = cleanupManager.cleanup();

        assertFalse(subDir.toFile().exists());
        assertEquals(1, result.getDeletedCount());
    }

    @Test
    void testCleanupNonExistentPathIsIgnored() {
        Path ghost = tempDir.resolve("nonexistent.tmp");
        cleanupManager.register(ghost);
        CleanupResult result = cleanupManager.cleanup();

        assertEquals(0, result.getDeletedCount());
        assertTrue(result.getFailedPaths().isEmpty());
    }

    @Test
    void testRegisteredPathsClearedAfterCleanup() throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "delta", ".patch");
        cleanupManager.register(tempFile);
        cleanupManager.cleanup();

        assertTrue(cleanupManager.getRegisteredPaths().isEmpty());
    }

    @Test
    void testResetDoesNotDeleteFiles() throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "keep", ".apk");
        cleanupManager.register(tempFile);
        cleanupManager.reset();

        assertTrue(tempFile.toFile().exists(), "File should still exist after reset");
        assertTrue(cleanupManager.getRegisteredPaths().isEmpty());
    }

    @Test
    void testAutoCleanupEnabledFlag() {
        assertTrue(cleanupManager.isAutoCleanupEnabled());
        cleanupManager.setAutoCleanupEnabled(false);
        assertFalse(cleanupManager.isAutoCleanupEnabled());
    }

    @Test
    void testRegisterNullIsIgnored() {
        assertDoesNotThrow(() -> cleanupManager.register(null));
        assertTrue(cleanupManager.getRegisteredPaths().isEmpty());
    }
}
