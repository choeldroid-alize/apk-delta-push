package com.apkdeltapush.diagnostics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Collects diagnostic data points during a push session and produces a {@link PushDiagnosticsReport}.
 */
public class PushDiagnosticsCollector {

    private static final Logger LOGGER = Logger.getLogger(PushDiagnosticsCollector.class.getName());

    private final String sessionId;
    private final String deviceSerial;
    private final Map<String, String> entries = new LinkedHashMap<>();
    private DiagnosticsStatus status = DiagnosticsStatus.OK;

    public PushDiagnosticsCollector(String sessionId, String deviceSerial) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId must not be blank");
        }
        if (deviceSerial == null || deviceSerial.isBlank()) {
            throw new IllegalArgumentException("deviceSerial must not be blank");
        }
        this.sessionId = sessionId;
        this.deviceSerial = deviceSerial;
    }

    public void record(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Diagnostic key must not be blank");
        }
        entries.put(key, value != null ? value : "<null>");
        LOGGER.fine("[" + sessionId + "] diagnostic recorded: " + key + "=" + value);
    }

    public void recordError(String key, String errorDetail) {
        record(key, errorDetail);
        status = DiagnosticsStatus.DEGRADED;
    }

    public void markFailed(String reason) {
        record("failure_reason", reason);
        status = DiagnosticsStatus.FAILED;
    }

    public void recordSystemInfo() {
        record("os_name", System.getProperty("os.name"));
        record("os_version", System.getProperty("os.version"));
        record("java_version", System.getProperty("java.version"));
        record("available_processors", String.valueOf(Runtime.getRuntime().availableProcessors()));
        record("free_memory_bytes", String.valueOf(Runtime.getRuntime().freeMemory()));
    }

    public PushDiagnosticsReport buildReport() {
        LOGGER.info("Building diagnostics report for session " + sessionId + " status=" + status);
        return new PushDiagnosticsReport(sessionId, deviceSerial, entries, status);
    }

    public DiagnosticsStatus getCurrentStatus() {
        return status;
    }

    public int entryCount() {
        return entries.size();
    }
}
