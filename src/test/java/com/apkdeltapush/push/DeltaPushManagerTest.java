package com.apkdeltapush.push;

import com.apkdeltapush.adb.AdbClient;
import com.apkdeltapush.diff.ApkDiffGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeltaPushManagerTest {

    private AdbClient adbClient;
    private ApkDiffGenerator diffGenerator;
    private DeltaPushManager manager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        adbClient = Mockito.mock(AdbClient.class);
        diffGenerator = Mockito.mock(ApkDiffGenerator.class);
        manager = new DeltaPushManager(adbClient, diffGenerator);
    }

    @Test
    void push_deltaSmaller_usesPatchPath() throws IOException {
        File oldApk = createFakeApk("old.apk", 1000);
        File newApk = createFakeApk("new.apk", 1000);

        // Simulate diffGenerator writing a small patch
        doAnswer(invocation -> {
            File patch = invocation.getArgument(2);
            Files.write(patch.toPath(), new byte[200]);
            return null;
        }).when(diffGenerator).generate(eq(oldApk), eq(newApk), any(File.class));

        PushResult result = manager.push("emulator-5554", oldApk, newApk);

        assertTrue(result.isDeltaUsed());
        assertEquals(200, result.getBytesTransferred());
        verify(adbClient).pushFile(eq("emulator-5554"), any(File.class), anyString());
        verify(adbClient).applyPatch(eq("emulator-5554"), anyString());
        verify(adbClient, never()).installApk(anyString(), any(File.class));
    }

    @Test
    void push_deltaLarger_fallsBackToFullInstall() throws IOException {
        File oldApk = createFakeApk("old.apk", 100);
        File newApk = createFakeApk("new.apk", 100);

        doAnswer(invocation -> {
            File patch = invocation.getArgument(2);
            Files.write(patch.toPath(), new byte[500]); // larger than APK
            return null;
        }).when(diffGenerator).generate(eq(oldApk), eq(newApk), any(File.class));

        PushResult result = manager.push("emulator-5554", oldApk, newApk);

        assertFalse(result.isDeltaUsed());
        verify(adbClient).installApk(eq("emulator-5554"), eq(newApk));
        verify(adbClient, never()).pushFile(anyString(), any(File.class), anyString());
    }

    @Test
    void push_missingOldApk_throwsIllegalArgument() {
        File missing = new File(tempDir.toFile(), "ghost.apk");
        File newApk = createFakeApk("new.apk", 100);
        assertThrows(IllegalArgumentException.class,
                () -> manager.push("emulator-5554", missing, newApk));
    }

    private File createFakeApk(String name, int size) {
        try {
            Path p = tempDir.resolve(name);
            Files.write(p, new byte[size]);
            return p.toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
