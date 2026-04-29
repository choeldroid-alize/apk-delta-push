package com.apkdeltapush.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeltaDiffMetricsExporterTest {

    private DeltaDiffMetrics metrics;
    private DeltaDiffMetricsExporter exporter;

    @BeforeEach
    void setUp() {
        metrics = new DeltaDiffMetrics();
        exporter = new DeltaDiffMetricsExporter(metrics);
    }

    @Test
    void constructorRejectsNullMetrics() {
        assertThrows(NullPointerException.class, () -> new DeltaDiffMetricsExporter(null));
    }

    @Test
    void exportToWriterProducesExpectedKeys() {
        metrics.recordDiff(2000, 500, 80);
        metrics.recordFailure();

        StringWriter sw = new StringWriter();
        exporter.exportToWriter(new PrintWriter(sw));
        String output = sw.toString();

        assertTrue(output.contains("diffs_computed=1"));
        assertTrue(output.contains("diffs_failed=1"));
        assertTrue(output.contains("bytes_in=2000"));
        assertTrue(output.contains("bytes_out=500"));
        assertTrue(output.contains("total_compute_time_ms=80"));
        assertTrue(output.contains("avg_compute_time_ms="));
        assertTrue(output.contains("compression_ratio="));
        assertTrue(output.contains("last_diff_timestamp="));
    }

    @Test
    void exportToFileWritesReadableContent(@TempDir Path tempDir) throws IOException {
        metrics.recordDiff(4096, 1024, 120);
        Path out = tempDir.resolve("metrics.txt");
        exporter.exportToFile(out);

        assertTrue(Files.exists(out));
        List<String> lines = Files.readAllLines(out);
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("diffs_computed=")));
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("compression_ratio=")));
    }

    @Test
    void exportToFileRejectsNullPath() {
        assertThrows(NullPointerException.class, () -> exporter.exportToFile(null));
    }

    @Test
    void exportToWriterRejectsNullWriter() {
        assertThrows(NullPointerException.class, () -> exporter.exportToWriter(null));
    }

    @Test
    void noDataExportShowsZeroValues() {
        StringWriter sw = new StringWriter();
        exporter.exportToWriter(new PrintWriter(sw));
        String output = sw.toString();

        assertTrue(output.contains("diffs_computed=0"));
        assertTrue(output.contains("last_diff_timestamp=N/A"));
    }
}
