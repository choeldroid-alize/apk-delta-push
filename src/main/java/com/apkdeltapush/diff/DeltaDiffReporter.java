package com.apkdeltapush.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Builds and records {@link DeltaDiffReport} instances produced during diff
 * operations. Provides summary formatting suitable for CLI output and logging.
 */
public class DeltaDiffReporter {

    private static final Logger LOGGER = Logger.getLogger(DeltaDiffReporter.class.getName());

    private static final double LARGE_DELTA_THRESHOLD = 0.80; // warn if delta > 80% of target
    private static final long SLOW_DIFF_THRESHOLD_MS = 10_000L;

    private final List<DeltaDiffReport> history = new ArrayList<>();

    /**
     * Builds a {@link DeltaDiffReport} from raw diff inputs, applying automatic
     * warning detection, and stores it in the reporter's history.
     */
    public DeltaDiffReport buildAndRecord(
            String sourceApkPath,
            String targetApkPath,
            long sourceSizeBytes,
            long targetSizeBytes,
            long deltaSizeBytes,
            DeltaDiffStrategy strategy,
            long durationMillis,
            boolean success) {

        Objects.requireNonNull(sourceApkPath, "sourceApkPath");
        Objects.requireNonNull(targetApkPath, "targetApkPath");
        Objects.requireNonNull(strategy, "strategy");

        List<String> warnings = new ArrayList<>();

        if (targetSizeBytes > 0 && (double) deltaSizeBytes / targetSizeBytes > LARGE_DELTA_THRESHOLD) {
            warnings.add(String.format(
                    "Delta size (%.1f%% of target) exceeds threshold; consider full APK push.",
                    100.0 * deltaSizeBytes / targetSizeBytes));
        }

        if (durationMillis > SLOW_DIFF_THRESHOLD_MS) {
            warnings.add(String.format(
                    "Diff generation took %d ms, which exceeds the %d ms threshold.",
                    durationMillis, SLOW_DIFF_THRESHOLD_MS));
        }

        if (!success) {
            warnings.add("Diff operation completed with a failure status.");
        }

        DeltaDiffReport report = new DeltaDiffReport(
                sourceApkPath, targetApkPath,
                sourceSizeBytes, targetSizeBytes, deltaSizeBytes,
                strategy, durationMillis, warnings, success);

        history.add(report);
        LOGGER.info(() -> "Recorded diff report: " + report);
        return report;
    }

    /** Returns an unmodifiable view of all recorded reports in this session. */
    public List<DeltaDiffReport> getHistory() {
        return java.util.Collections.unmodifiableList(history);
    }

    /** Formats a human-readable summary of the given report. */
    public String formatSummary(DeltaDiffReport report) {
        Objects.requireNonNull(report, "report");
        StringBuilder sb = new StringBuilder();
        sb.append("=== Delta Diff Report ===\n");
        sb.append(String.format("  Source  : %s (%d bytes)%n", report.getSourceApkPath(), report.getSourceSizeBytes()));
        sb.append(String.format("  Target  : %s (%d bytes)%n", report.getTargetApkPath(), report.getTargetSizeBytes()));
        sb.append(String.format("  Delta   : %d bytes (%.1f%% savings)%n",
                report.getDeltaSizeBytes(), report.getCompressionRatio() * 100));
        sb.append(String.format("  Strategy: %s%n", report.getStrategy()));
        sb.append(String.format("  Duration: %d ms%n", report.getDurationMillis()));
        sb.append(String.format("  Status  : %s%n", report.isSuccess() ? "SUCCESS" : "FAILED"));
        if (report.hasWarnings()) {
            sb.append("  Warnings:\n");
            report.getWarnings().forEach(w -> sb.append("    - ").append(w).append("\n"));
        }
        return sb.toString();
    }

    /** Clears all recorded history. */
    public void clearHistory() {
        history.clear();
    }
}
