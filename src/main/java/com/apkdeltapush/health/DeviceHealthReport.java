package com.apkdeltapush.health;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * Immutable report produced by {@link DeviceHealthChecker} for a single device.
 */
public class DeviceHealthReport {

    private final String deviceSerial;
    private final boolean healthy;
    private final String message;
    private final Map<String, String> metrics;
    private final Instant timestamp;

    public DeviceHealthReport(String deviceSerial, boolean healthy,
                              String message, Map<String, String> metrics) {
        this.deviceSerial = deviceSerial;
        this.healthy = healthy;
        this.message = message;
        this.metrics = metrics != null ? Collections.unmodifiableMap(metrics) : Collections.emptyMap();
        this.timestamp = Instant.now();
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getMetrics() {
        return metrics;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "DeviceHealthReport{" +
                "deviceSerial='" + deviceSerial + '\'' +
                ", healthy=" + healthy +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
