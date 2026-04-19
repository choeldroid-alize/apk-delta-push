package com.apkdeltapush.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PushEventLoggerTest {

    private PushEventLogger logger;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        logger = new PushEventLogger(tempDir.resolve("push.log"), false);
    }

    @Test
    void testLogAddsEventToMemory() {
        logger.log(PushEventType.SESSION_START, "emulator-5554", "Session started");
        List<PushEvent> events = logger.getEvents();
        assertEquals(1, events.size());
        assertEquals(PushEventType.SESSION_START, events.get(0).getType());
        assertEquals("emulator-5554", events.get(0).getDeviceSerial());
    }

    @Test
    void testGetEventsByDevice() {
        logger.log(PushEventType.INSTALL_SUCCESS, "device-A", "Installed");
        logger.log(PushEventType.INSTALL_FAILURE, "device-B", "Failed");
        logger.log(PushEventType.RETRY_ATTEMPT, "device-A", "Retrying");

        List<PushEvent> deviceA = logger.getEventsByDevice("device-A");
        assertEquals(2, deviceA.size());
    }

    @Test
    void testClearRemovesAllEvents() {
        logger.log(PushEventType.DIFF_GENERATED, "device-A", "Diff ready");
        logger.clear();
        assertTrue(logger.getEvents().isEmpty());
    }

    @Test
    void testPersistToDisk() throws IOException {
        Path logPath = tempDir.resolve("events.log");
        PushEventLogger diskLogger = new PushEventLogger(logPath, true);
        diskLogger.log(PushEventType.PATCH_APPLIED, "emulator-5554", "Patch OK");
        assertTrue(Files.exists(logPath));
        String content = Files.readString(logPath);
        assertTrue(content.contains("PATCH_APPLIED"));
        assertTrue(content.contains("emulator-5554"));
    }

    @Test
    void testExporterFilterByType() {
        logger.log(PushEventType.INSTALL_SUCCESS, "dev1", "ok");
        logger.log(PushEventType.INSTALL_FAILURE, "dev2", "fail");
        logger.log(PushEventType.INSTALL_SUCCESS, "dev3", "ok");

        PushEventLogExporter exporter = new PushEventLogExporter(logger);
        List<PushEvent> successes = exporter.filterByType(PushEventType.INSTALL_SUCCESS);
        assertEquals(2, successes.size());
    }
}
