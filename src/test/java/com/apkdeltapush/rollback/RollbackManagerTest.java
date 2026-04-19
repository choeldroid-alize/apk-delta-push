package com.apkdeltapush.rollback;

import com.apkdeltapush.adb.AdbClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RollbackManagerTest {

    private AdbClient adbClient;
    private RollbackManager rollbackManager;

    private static final String SERIAL = "emulator-5554";
    private static final String PACKAGE = "com.example.app";
    private static final String APK_PATH = "/data/app/com.example.app/base.apk";

    @BeforeEach
    void setUp() {
        adbClient = Mockito.mock(AdbClient.class);
        rollbackManager = new RollbackManager(adbClient);
    }

    @Test
    void createBackup_success() throws IOException {
        when(adbClient.executeShellCommand(eq(SERIAL), anyString())).thenReturn("");

        boolean result = rollbackManager.createBackup(SERIAL, PACKAGE, APK_PATH);

        assertTrue(result);
        assertTrue(rollbackManager.hasBackup(SERIAL, PACKAGE));
        verify(adbClient, atLeastOnce()).executeShellCommand(eq(SERIAL), contains("cp"));
    }

    @Test
    void createBackup_failsOnException() throws IOException {
        when(adbClient.executeShellCommand(eq(SERIAL), anyString()))
                .thenThrow(new IOException("adb error"));

        boolean result = rollbackManager.createBackup(SERIAL, PACKAGE, APK_PATH);

        assertFalse(result);
        assertFalse(rollbackManager.hasBackup(SERIAL, PACKAGE));
    }

    @Test
    void rollback_success() throws IOException {
        when(adbClient.executeShellCommand(eq(SERIAL), anyString())).thenReturn("");
        rollbackManager.createBackup(SERIAL, PACKAGE, APK_PATH);

        boolean result = rollbackManager.rollback(SERIAL, PACKAGE);

        assertTrue(result);
        assertFalse(rollbackManager.hasBackup(SERIAL, PACKAGE));
        verify(adbClient, atLeastOnce()).executeShellCommand(eq(SERIAL), contains("pm install"));
    }

    @Test
    void rollback_noBackupReturnsFalse() {
        boolean result = rollbackManager.rollback(SERIAL, PACKAGE);
        assertFalse(result);
    }

    @Test
    void clearBackup_removesEntry() throws IOException {
        when(adbClient.executeShellCommand(eq(SERIAL), anyString())).thenReturn("");
        rollbackManager.createBackup(SERIAL, PACKAGE, APK_PATH);

        rollbackManager.clearBackup(SERIAL, PACKAGE);

        assertFalse(rollbackManager.hasBackup(SERIAL, PACKAGE));
        verify(adbClient, atLeastOnce()).executeShellCommand(eq(SERIAL), contains("rm -f"));
    }
}
