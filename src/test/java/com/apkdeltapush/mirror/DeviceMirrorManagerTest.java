package com.apkdeltapush.mirror;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeviceMirrorManagerTest {

    private MirrorPushDelegate delegate;
    private DeviceMirrorManager manager;

    @BeforeEach
    void setUp() {
        delegate = mock(MirrorPushDelegate.class);
        manager = new DeviceMirrorManager(delegate);
    }

    @Test
    void mirrorToMultipleDevices_allSucceed() throws Exception {
        when(delegate.hasSameVersion(any(), any(), any())).thenReturn(false);
        when(delegate.pushDelta(any(), any(), any())).thenReturn(true);

        DeviceMirrorConfig config = DeviceMirrorConfig.builder("srcDevice")
                .targetDeviceIds(Arrays.asList("dev1", "dev2", "dev3"))
                .build();

        DeviceMirrorResult result = manager.mirror("/path/to/app.apk", config);

        assertTrue(result.isFullSuccess());
        assertEquals(3, result.successCount());
        assertTrue(result.getFailedDevices().isEmpty());
    }

    @Test
    void mirrorToMultipleDevices_partialFailure() throws Exception {
        when(delegate.hasSameVersion(any(), any(), any())).thenReturn(false);
        when(delegate.pushDelta(any(), eq("srcDevice"), eq("dev1"))).thenReturn(true);
        when(delegate.pushDelta(any(), eq("srcDevice"), eq("dev2"))).thenReturn(false);

        DeviceMirrorConfig config = DeviceMirrorConfig.builder("srcDevice")
                .targetDeviceIds(Arrays.asList("dev1", "dev2"))
                .build();

        DeviceMirrorResult result = manager.mirror("/path/to/app.apk", config);

        assertFalse(result.isFullSuccess());
        assertTrue(result.isPartialSuccess());
        assertEquals(1, result.getFailedDevices().size());
        assertEquals("dev2", result.getFailedDevices().get(0));
    }

    @Test
    void mirrorSkipsDeviceWithIdenticalVersion() throws Exception {
        when(delegate.hasSameVersion(any(), eq("dev1"), any())).thenReturn(true);
        when(delegate.hasSameVersion(any(), eq("dev2"), any())).thenReturn(false);
        when(delegate.pushDelta(any(), any(), eq("dev2"))).thenReturn(true);

        DeviceMirrorConfig config = DeviceMirrorConfig.builder("srcDevice")
                .targetDeviceIds(Arrays.asList("dev1", "dev2"))
                .skipIdenticalVersions(true)
                .build();

        DeviceMirrorResult result = manager.mirror("/path/to/app.apk", config);

        verify(delegate, never()).pushDelta(any(), any(), eq("dev1"));
        assertTrue(result.isFullSuccess());
    }

    @Test
    void mirrorWithNoTargets_returnsEmptyResult() {
        DeviceMirrorConfig config = DeviceMirrorConfig.builder("srcDevice")
                .targetDeviceIds(Collections.emptyList())
                .build();

        DeviceMirrorResult result = manager.mirror("/path/to/app.apk", config);

        assertEquals(0, result.totalTargets());
        assertFalse(result.isFullSuccess());
    }

    @Test
    void nullApkPath_throwsNullPointerException() {
        DeviceMirrorConfig config = DeviceMirrorConfig.builder("srcDevice")
                .targetDeviceIds(Collections.singletonList("dev1"))
                .build();
        assertThrows(NullPointerException.class, () -> manager.mirror(null, config));
    }
}
