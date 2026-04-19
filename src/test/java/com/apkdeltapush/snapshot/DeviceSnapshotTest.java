package com.apkdeltapush.snapshot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeviceSnapshotTest {

    @Test
    void constructorAndGetters() {
        DeviceSnapshot snap = new DeviceSnapshot("serial1", "com.app", "10", "deadbeef", 1000L);
        assertEquals("serial1", snap.getDeviceSerial());
        assertEquals("com.app", snap.getPackageName());
        assertEquals("10", snap.getVersionCode());
        assertEquals("deadbeef", snap.getChecksum());
        assertEquals(1000L, snap.getCapturedAtMs());
    }

    @Test
    void toStringContainsKeyFields() {
        DeviceSnapshot snap = new DeviceSnapshot("serial1", "com.app", "10", "deadbeef", 1000L);
        String str = snap.toString();
        assertTrue(str.contains("serial1"));
        assertTrue(str.contains("com.app"));
        assertTrue(str.contains("10"));
        assertTrue(str.contains("deadbeef"));
    }
}
