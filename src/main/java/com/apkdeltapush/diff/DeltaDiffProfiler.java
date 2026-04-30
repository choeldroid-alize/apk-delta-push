package com.apkdeltapush.diff;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Profiles the timing and resource usage of individual diff operations,
 * enabling performance analysis and bottleneck identification.
 */
public class DeltaDiffProfiler {

    private final Map<String, Instant> startTimes = new ConcurrentHashMap<>();
    private final Map<String, DeltaDiffProfileEntry> entries = new LinkedHashMap<>();

    /**
     * Marks the start of a named diff phase.
     *
     * @param phase identifier for the phase (e.g. "bsdiff", "compress", "checksum")
     */
    public void startPhase(String phase) {
        if (phase == null || phase.isBlank()) {
            throw new IllegalArgumentException("Phase name must not be null or blank");
        }
        startTimes.put(phase, Instant.now());
    }

    /**
     * Marks the end of a named diff phase and records its duration and byte metrics.
     *
     * @param phase       identifier matching a prior {@link #startPhase(String)} call
     * @param inputBytes  number of input bytes processed during this phase
     * @param outputBytes number of output bytes produced during this phase
     */
    public void endPhase(String phase, long inputBytes, long outputBytes) {
        Instant start = startTimes.remove(phase);
        if (start == null) {
            throw new IllegalStateException("Phase '" + phase + "' was not started");
        }
        Duration elapsed = Duration.between(start, Instant.now());
        entries.put(phase, new DeltaDiffProfileEntry(phase, elapsed, inputBytes, outputBytes));
    }

    /**
     * Returns an immutable snapshot of all completed phase entries, ordered by insertion.
     */
    public Map<String, DeltaDiffProfileEntry> getEntries() {
        return Map.copyOf(entries);
    }

    /**
     * Returns the total elapsed time across all completed phases.
     */
    public Duration getTotalElapsed() {
        return entries.values().stream()
                .map(DeltaDiffProfileEntry::getElapsed)
                .reduce(Duration.ZERO, Duration::plus);
    }

    /**
     * Returns the overall compression ratio (total output / total input) across all phases.
     * Returns 1.0 if no input bytes were recorded.
     */
    public double getOverallCompressionRatio() {
        long totalIn = entries.values().stream().mapToLong(DeltaDiffProfileEntry::getInputBytes).sum();
        long totalOut = entries.values().stream().mapToLong(DeltaDiffProfileEntry::getOutputBytes).sum();
        return totalIn == 0 ? 1.0 : (double) totalOut / totalIn;
    }

    /** Clears all recorded entries and in-progress phase markers. */
    public void reset() {
        startTimes.clear();
        entries.clear();
    }
}
