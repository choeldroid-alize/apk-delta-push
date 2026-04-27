package com.apkdeltapush.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeltaDiffPatcherTest {

    @TempDir
    Path tempDir;

    private DeltaDiffPatcher patcher;
    private DeltaDiffPatchOptions options;

    @BeforeEach
    void setUp() {
        options = DeltaDiffPatchOptions.builder()
                .validateOutputSize(false)
                .overwriteExisting(true)
                .build();
        patcher = new DeltaDiffPatcher(options);
    }

    @Test
    void patch_writesOutputFile() throws Exception {
        Path baseApk = tempDir.resolve("base.apk");
        byte[] baseBytes = new byte[]{0x01, 0x02, 0x03, 0x04};
        Files.write(baseApk, baseBytes);

        byte[] deltaBytes = new byte[]{0x11, 0x12, 0x13, 0x14};
        DeltaDiffResult diffResult = mock(DeltaDiffResult.class);
        when(diffResult.getDeltaBytes()).thenReturn(deltaBytes);
        when(diffResult.getTargetSize()).thenReturn(0L);

        Path output = tempDir.resolve("patched.apk");
        DeltaDiffPatchResult result = patcher.patch(baseApk, diffResult, output);

        assertTrue(result.isSuccess());
        assertTrue(Files.exists(output));
        assertEquals(deltaBytes.length, result.getOutputSizeBytes());
    }

    @Test
    void patch_throwsWhenBaseApkMissing() {
        Path missing = tempDir.resolve("nonexistent.apk");
        DeltaDiffResult diffResult = mock(DeltaDiffResult.class);
        when(diffResult.getDeltaBytes()).thenReturn(new byte[]{0x01});

        assertThrows(DeltaDiffPatchException.class,
                () -> patcher.patch(missing, diffResult, tempDir.resolve("out.apk")));
    }

    @Test
    void patch_throwsWhenDeltaBytesEmpty() throws IOException {
        Path baseApk = tempDir.resolve("base.apk");
        Files.write(baseApk, new byte[]{0x01});

        DeltaDiffResult diffResult = mock(DeltaDiffResult.class);
        when(diffResult.getDeltaBytes()).thenReturn(new byte[0]);

        assertThrows(DeltaDiffPatchException.class,
                () -> patcher.patch(baseApk, diffResult, tempDir.resolve("out.apk")));
    }

    @Test
    void patch_validateOutputSizeThrowsOnMismatch() throws IOException {
        DeltaDiffPatchOptions strictOptions = DeltaDiffPatchOptions.builder()
                .validateOutputSize(true)
                .build();
        DeltaDiffPatcher strictPatcher = new DeltaDiffPatcher(strictOptions);

        Path baseApk = tempDir.resolve("base2.apk");
        Files.write(baseApk, new byte[]{0x01, 0x02});

        DeltaDiffResult diffResult = mock(DeltaDiffResult.class);
        when(diffResult.getDeltaBytes()).thenReturn(new byte[]{0x11, 0x12});
        when(diffResult.getTargetSize()).thenReturn(999L); // deliberate mismatch

        assertThrows(DeltaDiffPatchException.class,
                () -> strictPatcher.patch(baseApk, diffResult, tempDir.resolve("out2.apk")));
    }

    @Test
    void defaultOptions_hasExpectedDefaults() {
        DeltaDiffPatchOptions defaults = DeltaDiffPatchOptions.defaults();
        assertTrue(defaults.isValidateOutputSize());
        assertFalse(defaults.isOverwriteExisting());
        assertEquals(8192, defaults.getBufferSizeBytes());
    }

    @Test
    void patchResult_toStringContainsFields() {
        Path p = tempDir.resolve("x.apk");
        DeltaDiffPatchResult r = new DeltaDiffPatchResult(p, 1024L, 50L, true);
        String s = r.toString();
        assertTrue(s.contains("outputSizeBytes=1024"));
        assertTrue(s.contains("success=true"));
    }
}
