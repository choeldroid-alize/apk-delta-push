package com.apkdeltapush.patch;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class PatchApplierTest {

    private PatchApplier applier;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        applier = new PatchApplier();
    }

    @Test
    void applyDelta_producesExpectedOutput() {
        byte[] base  = {0x10, 0x20, 0x30};
        // header encodes outputLen = 3, then XOR bytes
        byte[] patch = {0x00, 0x00, 0x00, 0x03, 0x01, 0x02, 0x03};
        byte[] result = applier.applyDelta(base, patch);
        assertArrayEquals(new byte[]{0x11, 0x22, 0x33}, result);
    }

    @Test
    void applyDelta_throwsOnShortPatch() {
        assertThrows(IllegalArgumentException.class,
                () -> applier.applyDelta(new byte[]{1, 2}, new byte[]{0, 0}));
    }

    @Test
    void apply_throwsWhenBaseApkMissing() {
        Path missing = tempDir.resolve("missing.apk");
        Path patch   = tempDir.resolve("patch.bin");
        Path output  = tempDir.resolve("out.apk");
        assertThrows(FileNotFoundException.class,
                () -> applier.apply(missing, patch, output));
    }

    @Test
    void apply_throwsWhenPatchFileMissing() throws Exception {
        Path base   = tempDir.resolve("base.apk");
        Files.write(base, new byte[]{0x10, 0x20});
        Path missing = tempDir.resolve("no.patch");
        Path output  = tempDir.resolve("out.apk");
        assertThrows(FileNotFoundException.class,
                () -> applier.apply(base, missing, output));
    }

    @Test
    void apply_writesOutputFile() throws Exception {
        Path base   = tempDir.resolve("base.apk");
        Path patch  = tempDir.resolve("patch.bin");
        Path output = tempDir.resolve("out.apk");
        Files.write(base,  new byte[]{0x10, 0x20, 0x30});
        Files.write(patch, new byte[]{0x00, 0x00, 0x00, 0x03, 0x01, 0x02, 0x03});
        applier.apply(base, patch, output);
        assertTrue(Files.exists(output));
        assertArrayEquals(new byte[]{0x11, 0x22, 0x33}, Files.readAllBytes(output));
    }
}
