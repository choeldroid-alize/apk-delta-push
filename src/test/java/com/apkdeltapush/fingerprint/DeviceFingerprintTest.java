package com.apkdeltapush.fingerprint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeviceFingerprintTest {

    @Test
    void constructor_validArgs_createsFingerprint() {
        DeviceFingerprint fp = new DeviceFingerprint("emulator-5554", "arm64-v8a", 33, "abc123");
        assertEquals("emulator-5554", fp.getDeviceSerial());
        assertEquals("arm64-v8a", fp.getAbi());
        assertEquals(33, fp.getSdkVersion());
        assertEquals("abc123", fp.getInstalledApkDigest());
    }

    @Test
    void constructor_nullDigest_treatedAsEmpty() {
        DeviceFingerprint fp = new DeviceFingerprint("emulator-5554", "x86_64", 30, null);
        assertEquals("", fp.getInstalledApkDigest());
    }

    @Test
    void constructor_blankSerial_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeviceFingerprint("  ", "arm64-v8a", 33, "abc"));
    }

    @Test
    void constructor_invalidSdk_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeviceFingerprint("serial", "arm64-v8a", 0, "abc"));
    }

    @Test
    void toCacheKey_containsAllParts() {
        DeviceFingerprint fp = new DeviceFingerprint("serial1", "armeabi-v7a", 28, "deadbeef");
        String key = fp.toCacheKey();
        assertTrue(key.contains("serial1"));
        assertTrue(key.contains("armeabi-v7a"));
        assertTrue(key.contains("28"));
        assertTrue(key.contains("deadbeef"));
    }

    @Test
    void equals_sameValues_returnsTrue() {
        DeviceFingerprint a = new DeviceFingerprint("s1", "arm64-v8a", 33, "hash");
        DeviceFingerprint b = new DeviceFingerprint("s1", "arm64-v8a", 33, "hash");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_differentDigest_returnsFalse() {
        DeviceFingerprint a = new DeviceFingerprint("s1", "arm64-v8a", 33, "hash1");
        DeviceFingerprint b = new DeviceFingerprint("s1", "arm64-v8a", 33, "hash2");
        assertNotEquals(a, b);
    }

    @Test
    void toString_containsSerial() {
        DeviceFingerprint fp = new DeviceFingerprint("mydevice", "x86", 29, "ff00");
        assertTrue(fp.toString().contains("mydevice"));
    }
}
