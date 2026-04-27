package com.apkdeltapush.diff;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Logs and manages audit entries for delta diff operations.
 * Provides querying and summarisation of diff history.
 */
public class DeltaDiffAuditLogger {

    private final CopyOnWriteArrayList<DeltaDiffAuditEntry> entries = new CopyOnWriteArrayList<>();
    private final int maxEntries;

    public DeltaDiffAuditLogger(int maxEntries) {
        if (maxEntries <= 0) throw new IllegalArgumentException("maxEntries must be positive");
        this.maxEntries = maxEntries;
    }

    public DeltaDiffAuditEntry record(String sourceApkPath, String targetApkPath,
                                      long sourceSizeBytes, long targetSizeBytes,
                                      long deltaSizeBytes, boolean success,
                                      String failureReason, String strategyUsed) {
        DeltaDiffAuditEntry entry = new DeltaDiffAuditEntry(
                UUID.randomUUID().toString(),
                sourceApkPath, targetApkPath,
                sourceSizeBytes, targetSizeBytes, deltaSizeBytes,
                Instant.now(), success, failureReason, strategyUsed
        );
        entries.add(entry);
        trimIfNeeded();
        return entry;
    }

    public List<DeltaDiffAuditEntry> getAllEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public List<DeltaDiffAuditEntry> getFailedEntries() {
        return entries.stream()
                .filter(e -> !e.isSuccess())
                .collect(Collectors.toList());
    }

    public List<DeltaDiffAuditEntry> getEntriesForSource(String sourceApkPath) {
        return entries.stream()
                .filter(e -> e.getSourceApkPath().equals(sourceApkPath))
                .collect(Collectors.toList());
    }

    public Optional<DeltaDiffAuditEntry> findById(String entryId) {
        return entries.stream().filter(e -> e.getEntryId().equals(entryId)).findFirst();
    }

    public double getAverageCompressionRatio() {
        return entries.stream()
                .filter(DeltaDiffAuditEntry::isSuccess)
                .mapToDouble(DeltaDiffAuditEntry::getCompressionRatio)
                .average()
                .orElse(0.0);
    }

    public int getTotalEntryCount() { return entries.size(); }

    public void clear() { entries.clear(); }

    private void trimIfNeeded() {
        while (entries.size() > maxEntries) {
            entries.remove(0);
        }
    }
}
