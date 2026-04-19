package com.apkdeltapush.snapshot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeviceSnapshotManagerTest {

    private DeviceSnapshotManager manager;

    @BeforeEach
    void setUp() {
        manager = new DeviceSnapshotManager();
    }

    @Test
    void recordAndRetrieveSnapshot() {
        manager.recordSnapshot("emulator-5554", "com.example.app", "42", "abc123");
        Optional<DeviceSnapshot> snap = manager.getSnapshot("emulator-5554", "com.example.app");
        assertTrue(snap.isPresent());
        assertEquals("42", snap.get().getVersionCode());
        assertEquals("abc123", snap.get().getChecksum());
    }

    @Test
    void getMissingSnapshotReturnsEmpty() {
        Optional<DeviceSnapshot> snap = manager.getSnapshot("emulator-5554", "com.missing.app");
        assertFalse(snap.isPresent());
    }

    @Test
    void isStaleReturnsTrueWhenChecksumDiffers() {
        manager.recordSnapshot("emulator-5554", "com.example.app", "42", "abc123");
        assertTrue(manager.isStale("emulator-5554", "com.example.app", "xyz999"));
    }

    @Test
    void isStaleReturnsFalseWhenChecksumMatches() {
        manager.recordSnapshot("emulator-5554", "com.example.app", "42", "abc123");
        assertFalse(manager.isStale("emulator-5554", "com.example.app", "abc123"));
    }

    @Test
    void isStaleReturnsFalseWhenNoSnapshot() {
        assertFalse(manager.isStale("emulator-5554", "com.unknown", "abc123"));
    }

    @Test
    void removeSnapshotDecreasesCount() {
        manager.recordSnapshot("emulator-5554", "com.example.app", "1", "chk");
        assertEquals(1, manager.snapshotCount());
        assertTrue(manager.removeSnapshot("emulator-5554", "com.example.app"));
        assertEquals(0, manager.snapshotCount());
    }

    @Test
    void removeNonExistentSnapshotReturnsFalse() {
        assertFalse(manager.removeSnapshot("emulator-5554", "com.ghost"));
    }

    @Test
    void overwritingSnapshotUpdatesChecksum() {
        manager.recordSnapshot("emulator-5554", "com.example.app", "1", "old");
        manager.recordSnapshot("emulator-5554", "com.example.app", "2", "new");
        assertEquals(1, manager.snapshotCount());
        assertEquals("new", manager.getSnapshot("emulator-5554", "com.example.app").get().getChecksum());
    }
}
