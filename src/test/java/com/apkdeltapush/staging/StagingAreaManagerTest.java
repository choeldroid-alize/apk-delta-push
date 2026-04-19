package com.apkdeltapush.staging;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StagingAreaManagerTest {

    @TempDir
    Path tempDir;

    private StagingAreaManager manager;

    @BeforeEach
    void setUp() {
        manager = new StagingAreaManager(tempDir);
    }

    @Test
    void allocate_createsStagingAreaWithCorrectMetadata() throws IOException {
        StagingArea area = manager.allocate("emulator-5554", "com.example.app");
        assertNotNull(area);
        assertEquals("emulator-5554", area.getDeviceSerial());
        assertEquals("com.example.app", area.getPackageName());
        assertNotNull(area.getId());
        assertTrue(area.getDirectory().toFile().exists());
    }

    @Test
    void get_returnsAllocatedArea() throws IOException {
        StagingArea area = manager.allocate("emulator-5554", "com.example.app");
        StagingArea retrieved = manager.get(area.getId());
        assertSame(area, retrieved);
    }

    @Test
    void get_returnsNullForUnknownId() {
        assertNull(manager.get("nonexistent-id"));
    }

    @Test
    void release_removesStagingAreaAndDeletesDirectory() throws IOException {
        StagingArea area = manager.allocate("emulator-5554", "com.example.app");
        Path dir = area.getDirectory();
        assertTrue(dir.toFile().exists());
        manager.release(area.getId());
        assertFalse(manager.exists(area.getId()));
        assertFalse(dir.toFile().exists());
    }

    @Test
    void activeCount_tracksAllocationsAndReleases() throws IOException {
        assertEquals(0, manager.activeCount());
        StagingArea a1 = manager.allocate("device-1", "com.app.one");
        StagingArea a2 = manager.allocate("device-2", "com.app.two");
        assertEquals(2, manager.activeCount());
        manager.release(a1.getId());
        assertEquals(1, manager.activeCount());
        manager.release(a2.getId());
        assertEquals(0, manager.activeCount());
    }

    @Test
    void allocate_multipleAreasHaveUniqueIds() throws IOException {
        StagingArea a1 = manager.allocate("device-1", "com.app");
        StagingArea a2 = manager.allocate("device-1", "com.app");
        assertNotEquals(a1.getId(), a2.getId());
    }
}
