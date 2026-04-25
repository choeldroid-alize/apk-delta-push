package com.apkdeltapush.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeltaDiffEngineTest {

    @TempDir
    Path tempDir;

    private DeltaDiffOptions options;
    private DeltaDiffEngine engine;

    @BeforeEach
    void setUp() {
        options = mock(DeltaDiffOptions.class);
        when(options.getStrategy()).thenReturn(DeltaDiffStrategy.BSDIFF);
        engine = new DeltaDiffEngine(options);
    }

    @Test
    void constructor_nullOptions_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeltaDiffEngine(null));
    }

    @Test
    void generate_nullOldApk_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> engine.generate(null, tempDir.resolve("new.apk"), tempDir));
    }

    @Test
    void generate_nullNewApk_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> engine.generate(tempDir.resolve("old.apk"), null, tempDir));
    }

    @Test
    void generate_nullOutputDir_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> engine.generate(tempDir.resolve("old.apk"), tempDir.resolve("new.apk"), null));
    }

    @Test
    void generate_missingOldApk_returnsFailureResult() throws IOException {
        Path newApk = createTempApk("new.apk", 512);
        Path missingOld = tempDir.resolve("missing_old.apk");

        DeltaDiffResult result = engine.generate(missingOld, newApk, tempDir);

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void generate_missingNewApk_returnsFailureResult() throws IOException {
        Path oldApk = createTempApk("old.apk", 512);
        Path missingNew = tempDir.resolve("missing_new.apk");

        DeltaDiffResult result = engine.generate(oldApk, missingNew, tempDir);

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void generate_outputDirCreatedWhenMissing() throws IOException {
        Path oldApk = createTempApk("old.apk", 1024);
        Path newApk = createTempApk("new.apk", 1100);
        Path nestedOutput = tempDir.resolve("output").resolve("nested");

        assertFalse(Files.exists(nestedOutput));
        // Even if bsdiff fails internally, directories should be created
        engine.generate(oldApk, newApk, nestedOutput);
        assertTrue(Files.isDirectory(nestedOutput));
    }

    @Test
    void deltaResult_compressionRatio_calculatedCorrectly() {
        DeltaDiffResult result = DeltaDiffResult.builder()
                .originalSize(1000)
                .deltaSize(400)
                .success(true)
                .strategy(DeltaDiffStrategy.BSDIFF)
                .build();

        assertEquals(0.6, result.getCompressionRatio(), 0.001);
    }

    @Test
    void deltaResult_zeroOriginalSize_returnsZeroRatio() {
        DeltaDiffResult result = DeltaDiffResult.builder()
                .originalSize(0)
                .deltaSize(0)
                .success(true)
                .strategy(DeltaDiffStrategy.BSDIFF)
                .build();

        assertEquals(0.0, result.getCompressionRatio(), 0.001);
    }

    private Path createTempApk(String name, int sizeBytes) throws IOException {
        Path file = tempDir.resolve(name);
        Files.write(file, new byte[sizeBytes]);
        return file;
    }
}
