package com.apkdeltapush.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PushHistoryManagerTest {

    private PushHistoryManager manager;

    @BeforeEach
    void setUp() {
        manager = new PushHistoryManager(10);
    }

    private PushHistoryRecord buildRecord(String id, String device, String pkg, boolean success) {
        return new PushHistoryRecord(id, device, pkg, "1.0", "2.0",
                1024L, success, success ? null : "ADB error",
                Instant.now(), 300L);
    }

    @Test
    void addRecord_shouldStoreRecord() {
        manager.addRecord(buildRecord("r1", "emulator-5554", "com.example", true));
        assertEquals(1, manager.size());
    }

    @Test
    void addRecord_nullRecord_shouldThrow() {
        assertThrows(NullPointerException.class, () -> manager.addRecord(null));
    }

    @Test
    void addRecord_exceedsCapacity_shouldEvictOldest() {
        PushHistoryManager smallManager = new PushHistoryManager(3);
        smallManager.addRecord(buildRecord("r1", "dev1", "pkg", true));
        smallManager.addRecord(buildRecord("r2", "dev1", "pkg", true));
        smallManager.addRecord(buildRecord("r3", "dev1", "pkg", true));
        smallManager.addRecord(buildRecord("r4", "dev1", "pkg", true));

        assertEquals(3, smallManager.size());
        assertFalse(smallManager.findById("r1").isPresent(), "Oldest record should be evicted");
        assertTrue(smallManager.findById("r4").isPresent());
    }

    @Test
    void getRecordsByDevice_shouldFilterCorrectly() {
        manager.addRecord(buildRecord("r1", "emulator-5554", "com.example", true));
        manager.addRecord(buildRecord("r2", "emulator-5556", "com.example", true));
        manager.addRecord(buildRecord("r3", "emulator-5554", "com.other", false));

        List<PushHistoryRecord> result = manager.getRecordsByDevice("emulator-5554");
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> "emulator-5554".equals(r.getDeviceSerial())));
    }

    @Test
    void getRecordsByPackage_shouldFilterCorrectly() {
        manager.addRecord(buildRecord("r1", "dev1", "com.example", true));
        manager.addRecord(buildRecord("r2", "dev1", "com.other", true));

        List<PushHistoryRecord> result = manager.getRecordsByPackage("com.example");
        assertEquals(1, result.size());
        assertEquals("com.example", result.get(0).getPackageName());
    }

    @Test
    void getFailedRecords_shouldReturnOnlyFailures() {
        manager.addRecord(buildRecord("r1", "dev1", "pkg", true));
        manager.addRecord(buildRecord("r2", "dev1", "pkg", false));
        manager.addRecord(buildRecord("r3", "dev1", "pkg", false));

        List<PushHistoryRecord> failed = manager.getFailedRecords();
        assertEquals(2, failed.size());
        assertTrue(failed.stream().noneMatch(PushHistoryRecord::isSuccess));
    }

    @Test
    void findById_existingRecord_shouldReturnRecord() {
        manager.addRecord(buildRecord("r1", "dev1", "pkg", true));
        Optional<PushHistoryRecord> found = manager.findById("r1");
        assertTrue(found.isPresent());
        assertEquals("r1", found.get().getRecordId());
    }

    @Test
    void findById_missingRecord_shouldReturnEmpty() {
        Optional<PushHistoryRecord> found = manager.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void clear_shouldRemoveAllRecords() {
        manager.addRecord(buildRecord("r1", "dev1", "pkg", true));
        manager.addRecord(buildRecord("r2", "dev1", "pkg", true));
        manager.clear();
        assertEquals(0, manager.size());
    }

    @Test
    void constructor_invalidMaxRecords_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> new PushHistoryManager(0));
        assertThrows(IllegalArgumentException.class, () -> new PushHistoryManager(-1));
    }
}
