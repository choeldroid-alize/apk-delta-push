package com.apkdeltapush.revert;

import com.apkdeltapush.adb.AdbClient;
import com.apkdeltapush.history.PushHistoryManager;
import com.apkdeltapush.history.PushHistoryRecord;
import com.apkdeltapush.rollback.RollbackManager;
import com.apkdeltapush.verify.InstallVerifier;
import com.apkdeltapush.verify.VerificationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushRevertManagerTest {

    private static final String DEVICE = "emulator-5554";
    private static final String PACKAGE = "com.example.app";
    private static final String VERSION = "1.2.3";
    private static final String APK_PATH = "/tmp/example-1.2.3.apk";

    @Mock AdbClient adbClient;
    @Mock RollbackManager rollbackManager;
    @Mock PushHistoryManager historyManager;
    @Mock InstallVerifier installVerifier;
    @Mock PushHistoryRecord historyRecord;

    private PushRevertManager manager;

    @BeforeEach
    void setUp() {
        manager = new PushRevertManager(adbClient, rollbackManager, historyManager, installVerifier);
    }

    @Test
    void revert_successPath_returnsSuccessResult() {
        when(historyManager.findLastSuccessful(DEVICE, PACKAGE)).thenReturn(Optional.of(historyRecord));
        when(historyRecord.getVersionName()).thenReturn(VERSION);
        when(historyRecord.getApkPath()).thenReturn(APK_PATH);
        when(rollbackManager.rollback(DEVICE, PACKAGE, APK_PATH)).thenReturn(true);
        when(installVerifier.verify(DEVICE, PACKAGE, VERSION))
                .thenReturn(VerificationResult.success(VERSION));

        RevertResult result = manager.revert(DEVICE, PACKAGE);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getRevertedToVersion()).isEqualTo(VERSION);
        assertThat(result.getDeviceSerial()).isEqualTo(DEVICE);
        assertThat(result.getPackageName()).isEqualTo(PACKAGE);
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    void revert_noHistory_returnsFailure() {
        when(historyManager.findLastSuccessful(DEVICE, PACKAGE)).thenReturn(Optional.empty());

        RevertResult result = manager.revert(DEVICE, PACKAGE);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("No previous successful push record found");
        verifyNoInteractions(rollbackManager, installVerifier);
    }

    @Test
    void revert_rollbackFails_returnsFailure() {
        when(historyManager.findLastSuccessful(DEVICE, PACKAGE)).thenReturn(Optional.of(historyRecord));
        when(historyRecord.getVersionName()).thenReturn(VERSION);
        when(historyRecord.getApkPath()).thenReturn(APK_PATH);
        when(rollbackManager.rollback(DEVICE, PACKAGE, APK_PATH)).thenReturn(false);

        RevertResult result = manager.revert(DEVICE, PACKAGE);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("Rollback execution failed");
        verifyNoInteractions(installVerifier);
    }

    @Test
    void revert_verificationFails_returnsFailure() {
        when(historyManager.findLastSuccessful(DEVICE, PACKAGE)).thenReturn(Optional.of(historyRecord));
        when(historyRecord.getVersionName()).thenReturn(VERSION);
        when(historyRecord.getApkPath()).thenReturn(APK_PATH);
        when(rollbackManager.rollback(DEVICE, PACKAGE, APK_PATH)).thenReturn(true);
        when(installVerifier.verify(DEVICE, PACKAGE, VERSION))
                .thenReturn(VerificationResult.failure("version mismatch"));

        RevertResult result = manager.revert(DEVICE, PACKAGE);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("version mismatch");
    }

    @Test
    void constructor_nullArguments_throwsNPE() {
        assertThatNullPointerException().isThrownBy(
                () -> new PushRevertManager(null, rollbackManager, historyManager, installVerifier));
        assertThatNullPointerException().isThrownBy(
                () -> new PushRevertManager(adbClient, null, historyManager, installVerifier));
        assertThatNullPointerException().isThrownBy(
                () -> new PushRevertManager(adbClient, rollbackManager, null, installVerifier));
        assertThatNullPointerException().isThrownBy(
                () -> new PushRevertManager(adbClient, rollbackManager, historyManager, null));
    }
}
