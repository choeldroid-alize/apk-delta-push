package com.apkdeltapush.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PushMetricsExporterTest {

    private PushMetricsExporter exporter;
    private PushMetricsSnapshot snapshot;

    @BeforeEach
    void setUp() {
        exporter = new PushMetricsExporter();
        snapshot = new PushMetricsSnapshot(
                "session-42",
                Instant.parse("2024-06-01T10:00:00Z"),
                2048,
                512,
                4,
                1,
                1024.0,
                Map.of("emulator-5554", 1024L, "device-XYZ", 1024L)
        );
    }

    @Test
    void exportText_containsSessionId() throws IOException {
        StringWriter sw = new StringWriter();
        exporter.exportText(snapshot, sw);
        assertTrue(sw.toString().contains("session-42"));
    }

    @Test
    void exportText_containsBytesSent() throws IOException {
        StringWriter sw = new StringWriter();
        exporter.exportText(snapshot, sw);
        assertTrue(sw.toString().contains("2048"));
    }

    @Test
    void exportText_containsPerDeviceInfo() throws IOException {
        StringWriter sw = new StringWriter();
        exporter.exportText(snapshot, sw);
        String out = sw.toString();
        assertTrue(out.contains("emulator-5554") || out.contains("device-XYZ"));
    }

    @Test
    void exportJson_isValidJsonStructure() throws IOException {
        StringWriter sw = new StringWriter();
        exporter.exportJson(snapshot, sw);
        String json = sw.toString();
        assertTrue(json.startsWith("{"));
        assertTrue(json.contains("\"sessionId\""));
        assertTrue(json.contains("\"totalBytesSent\""));
        assertTrue(json.contains("\"perDeviceBytesSent\""));
        assertTrue(json.endsWith("}\n"));
    }

    @Test
    void exportJson_containsCorrectValues() throws IOException {
        StringWriter sw = new StringWriter();
        exporter.exportJson(snapshot, sw);
        String json = sw.toString();
        assertTrue(json.contains("session-42"));
        assertTrue(json.contains("2048"));
        assertTrue(json.contains("512"));
    }
}
