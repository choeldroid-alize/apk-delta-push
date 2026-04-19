package com.apkdeltapush.cache;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DeltaCacheTest {

    @TempDir
    Path tempDir;

    private DeltaCache deltaCache;

    @BeforeEach
    void setUp() throws IOException {
        deltaCache = new DeltaCache(tempDir.resolve("delta-cache").toString());
    }

    @Test
    void testCacheMissReturnsNull() {
        File result = deltaCache.getCachedPatch("abc123", "def456");
        assertNull(result, "Expected null for a cache miss");
    }

    @Test
    void testStorePatchAndCacheHit() throws IOException {
        File patchFile = tempDir.resolve("test.patch").toFile();
        Files.writeString(patchFile.toPath(), "fake patch content");

        deltaCache.storePatch("abc123", "def456", patchFile);

        File cached = deltaCache.getCachedPatch("abc123", "def456");
        assertNotNull(cached, "Expected a cache hit after storing patch");
        assertTrue(cached.exists(), "Cached file should exist on disk");
    }

    @Test
    void testStorePatchWithNonExistentFileThrows() {
        File nonExistent = new File("/nonexistent/path/fake.patch");
        assertThrows(IllegalArgumentException.class,
                () -> deltaCache.storePatch("aaa", "bbb", nonExistent));
    }

    @Test
    void testClearCacheRemovesAllEntries() throws IOException {
        File patch1 = tempDir.resolve("p1.patch").toFile();
        File patch2 = tempDir.resolve("p2.patch").toFile();
        Files.writeString(patch1.toPath(), "data1");
        Files.writeString(patch2.toPath(), "data2");

        deltaCache.storePatch("src1", "tgt1", patch1);
        deltaCache.storePatch("src2", "tgt2", patch2);

        deltaCache.clearCache();

        assertNull(deltaCache.getCachedPatch("src1", "tgt1"));
        assertNull(deltaCache.getCachedPatch("src2", "tgt2"));
    }

    @Test
    void testDifferentChecksumPairsAreCachedSeparately() throws IOException {
        File patch = tempDir.resolve("patch.patch").toFile();
        Files.writeString(patch.toPath(), "delta");

        deltaCache.storePatch("v1", "v2", patch);

        assertNotNull(deltaCache.getCachedPatch("v1", "v2"));
        assertNull(deltaCache.getCachedPatch("v1", "v3"));
        assertNull(deltaCache.getCachedPatch("v0", "v2"));
    }
}
