package com.apkdeltapush.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DevicePushProfileTest {

    @Test
    void defaultValuesAreSet() {
        DevicePushProfile profile = new DevicePushProfile("device-001");
        assertEquals("device-001", profile.getDeviceId());
        assertEquals(512 * 1024, profile.getMaxChunkSizeBytes());
        assertEquals(1, profile.getPreferredParallelStreams());
        assertEquals(0L, profile.getAverageTransferRateBytesPerSec());
        assertTrue(profile.isCompressionEnabled());
        assertFalse(profile.isEncryptionEnabled());
    }

    @Test
    void nullDeviceIdThrows() {
        assertThrows(NullPointerException.class, () -> new DevicePushProfile(null));
    }

    @Test
    void setMaxChunkSizeBytes_valid() {
        DevicePushProfile profile = new DevicePushProfile("d1");
        profile.setMaxChunkSizeBytes(1024 * 1024);
        assertEquals(1024 * 1024, profile.getMaxChunkSizeBytes());
    }

    @Test
    void setMaxChunkSizeBytes_invalidThrows() {
        DevicePushProfile profile = new DevicePushProfile("d1");
        assertThrows(IllegalArgumentException.class, () -> profile.setMaxChunkSizeBytes(0));
        assertThrows(IllegalArgumentException.class, () -> profile.setMaxChunkSizeBytes(-1));
    }

    @Test
    void setPreferredParallelStreams_invalidThrows() {
        DevicePushProfile profile = new DevicePushProfile("d1");
        assertThrows(IllegalArgumentException.class, () -> profile.setPreferredParallelStreams(0));
    }

    @Test
    void customAttributesStoredAndRetrieved() {
        DevicePushProfile profile = new DevicePushProfile("d2");
        profile.setAttribute("region", "us-west");
        assertEquals("us-west", profile.getAttribute("region"));
        assertNull(profile.getAttribute("missing"));
    }

    @Test
    void getCustomAttributes_returnsDefensiveCopy() {
        DevicePushProfile profile = new DevicePushProfile("d3");
        profile.setAttribute("k", "v");
        profile.getCustomAttributes().put("injected", "bad");
        assertNull(profile.getAttribute("injected"));
    }

    @Test
    void toStringContainsDeviceId() {
        DevicePushProfile profile = new DevicePushProfile("device-xyz");
        assertTrue(profile.toString().contains("device-xyz"));
    }
}
