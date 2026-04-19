package com.apkdeltapush.split;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SplitApkHandlerTest {

    private SplitApkHandler handler;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        handler = new SplitApkHandler();
    }

    private File createApk(String name) throws IOException {
        File f = tempDir.resolve(name).toFile();
        f.createNewFile();
        return f;
    }

    @Test
    void loadSplitSet_returnsCorrectBaseAndSplits() throws IOException {
        createApk("base.apk");
        createApk("config.arm64.apk");
        createApk("config.en.apk");

        SplitApkSet set = handler.loadSplitSet(tempDir.toFile());

        assertNotNull(set);
        assertEquals("base.apk", set.getBaseApk().getName());
        assertEquals(2, set.getSplitApks().size());
    }

    @Test
    void loadSplitSet_throwsWhenNoBaseApk() throws IOException {
        createApk("config.arm64.apk");
        createApk("config.en.apk");

        assertThrows(IllegalStateException.class, () -> handler.loadSplitSet(tempDir.toFile()));
    }

    @Test
    void loadSplitSet_throwsOnNullDirectory() {
        assertThrows(IllegalArgumentException.class, () -> handler.loadSplitSet(null));
    }

    @Test
    void isSplitApkDirectory_trueForMultipleApks() throws IOException {
        createApk("base.apk");
        createApk("config.hdpi.apk");
        assertTrue(handler.isSplitApkDirectory(tempDir.toFile()));
    }

    @Test
    void isSplitApkDirectory_falseForSingleApk() throws IOException {
        createApk("base.apk");
        assertFalse(handler.isSplitApkDirectory(tempDir.toFile()));
    }

    @Test
    void allApks_includesBaseAndSplits() throws IOException {
        createApk("base.apk");
        createApk("config.arm64.apk");

        SplitApkSet set = handler.loadSplitSet(tempDir.toFile());
        List<File> all = handler.allApks(set);

        assertEquals(2, all.size());
        assertEquals("base.apk", all.get(0).getName());
    }
}
