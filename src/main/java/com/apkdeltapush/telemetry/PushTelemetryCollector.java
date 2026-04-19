package com.apkdeltapush.telemetry;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects telemetry data during APK delta push operations,
 * tracking timing, byte counts, and error rates per device.
 */
public class PushTelemetryCollector {

    private final Map<String, List<TelemetryEntry>> entriesByDevice = new ConcurrentHashMap<>();

    public void record(String deviceId, String phase, long bytesTransferred, boolean success) {
        TelemetryEntry entry = new TelemetryEntry(deviceId, phase, bytesTransferred, success, Instant.now());
        entriesByDevice.computeIfAbsent(deviceId, k -> Collections.synchronizedList(new ArrayList<>())).add(entry);
    }

    public List<TelemetryEntry> getEntries(String deviceId) {
        return Collections.unmodifiableList(
            entriesByDevice.getOrDefault(deviceId, Collections.emptyList())
        );
    }

    public TelemetrySummary summarize(String deviceId) {
        List<TelemetryEntry> entries = entriesByDevice.getOrDefault(deviceId, Collections.emptyList());
        long totalBytes = 0;
        int totalOps = entries.size();
        int failures = 0;
        for (TelemetryEntry e : entries) {
            totalBytes += e.getBytesTransferred();
            if (!e.isSuccess()) failures++;
        }
        double errorRate = totalOps == 0 ? 0.0 : (double) failures / totalOps;
        return new TelemetrySummary(deviceId, totalOps, totalBytes, failures, errorRate);
    }

    public void clear(String deviceId) {
        entriesByDevice.remove(deviceId);
    }

    public void clearAll() {
        entriesByDevice.clear();
    }
}
