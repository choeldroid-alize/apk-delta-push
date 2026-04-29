package com.apkdeltapush.diff;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/**
 * Exports {@link DeltaDiffMetrics} snapshots to a file or writer in a
 * simple key=value format suitable for CI dashboards and log aggregators.
 */
public class DeltaDiffMetricsExporter {

    private final DeltaDiffMetrics metrics;

    public DeltaDiffMetricsExporter(DeltaDiffMetrics metrics) {
        this.metrics = Objects.requireNonNull(metrics, "metrics must not be null");
    }

    /**
     * Exports current metrics to the given file path, overwriting existing content.
     */
    public void exportToFile(Path outputPath) throws IOException {
        Objects.requireNonNull(outputPath, "outputPath must not be null");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputPath))) {
            writeMetrics(writer);
        }
    }

    /**
     * Exports current metrics to the provided writer.
     */
    public void exportToWriter(PrintWriter writer) {
        Objects.requireNonNull(writer, "writer must not be null");
        writeMetrics(writer);
    }

    private void writeMetrics(PrintWriter writer) {
        writer.println("# DeltaDiffMetrics export — " + Instant.now());
        writer.println("diffs_computed=" + metrics.getTotalDiffsComputed());
        writer.println("diffs_failed=" + metrics.getTotalDiffsFailed());
        writer.println("bytes_in=" + metrics.getTotalBytesIn());
        writer.println("bytes_out=" + metrics.getTotalBytesOut());
        writer.println("total_compute_time_ms=" + metrics.getTotalComputeTimeMs());
        writer.printf("avg_compute_time_ms=%.4f%n", metrics.getAverageComputeTimeMs());
        writer.printf("compression_ratio=%.6f%n", metrics.getCompressionRatio());
        Instant last = metrics.getLastDiffTimestamp();
        writer.println("last_diff_timestamp=" + (last != null ? last.toString() : "N/A"));
        writer.flush();
    }
}
