package com.apkdeltapush.diagnostics;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable snapshot of diagnostic information captured during or after a push operation.
 */
public class PushDiagnosticsReport {

    private final String sessionId;
    private final String deviceSerial;
    private final Instant capturedAt;
    private final Map<String, String> entries;
    private final DiagnosticsStatus status;

    public PushDiagnosticsReport(String sessionId, String deviceSerial,
                                  Map<String, String> entries, DiagnosticsStatus status) {
        this.sessionId = sessionId;
        this.deviceSerial = deviceSerial;
        this.capturedAt = Instant.now();
        this.entries = Collections.unmodifiableMap(new LinkedHashMap<>(entries));
        this.status = status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public Map<String, String> getEntries() {
        return entries;
    }

    public DiagnosticsStatus getStatus() {
        return status;
    }

    public boolean hasEntry(String key) {
        return entries.containsKey(key);
    }

    public String getEntry(String key) {
        return entries.getOrDefault(key, null);
    }

    @Override
    public String toString() {
        return "PushDiagnosticsReport{sessionId='" + sessionId +
                "', device='" + deviceSerial +
                "', status=" + status +
                ", entries=" + entries.size() + "}";
    }
}
