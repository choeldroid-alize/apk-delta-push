package com.apkdeltapush.health;

import com.apkdeltapush.adb.AdbClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Checks device health metrics before and during a push operation.
 * Verifies battery level, available storage, and ADB connectivity.
 */
public class DeviceHealthChecker {

    private static final int MIN_BATTERY_PERCENT = 15;
    private static final long MIN_FREE_STORAGE_BYTES = 50 * 1024 * 1024L; // 50 MB

    private final AdbClient adbClient;

    public DeviceHealthChecker(AdbClient adbClient) {
        if (adbClient == null) {
            throw new IllegalArgumentException("AdbClient must not be null");
        }
        this.adbClient = adbClient;
    }

    /**
     * Performs a full health check on the specified device.
     *
     * @param deviceSerial the ADB serial of the target device
     * @return a {@link DeviceHealthReport} describing the health status
     */
    public DeviceHealthReport checkHealth(String deviceSerial) {
        if (deviceSerial == null || deviceSerial.isBlank()) {
            throw new IllegalArgumentException("Device serial must not be null or blank");
        }

        Map<String, String> metrics = new HashMap<>();

        boolean connected = adbClient.isDeviceConnected(deviceSerial);
        metrics.put("connected", String.valueOf(connected));

        if (!connected) {
            return new DeviceHealthReport(deviceSerial, false,
                    "Device is not connected via ADB", metrics);
        }

        int batteryLevel = adbClient.getBatteryLevel(deviceSerial);
        metrics.put("batteryPercent", String.valueOf(batteryLevel));

        long freeStorage = adbClient.getFreeStorageBytes(deviceSerial);
        metrics.put("freeStorageBytes", String.valueOf(freeStorage));

        if (batteryLevel < MIN_BATTERY_PERCENT) {
            return new DeviceHealthReport(deviceSerial, false,
                    "Battery too low: " + batteryLevel + "% (minimum " + MIN_BATTERY_PERCENT + "%)", metrics);
        }

        if (freeStorage < MIN_FREE_STORAGE_BYTES) {
            return new DeviceHealthReport(deviceSerial, false,
                    "Insufficient storage: " + freeStorage + " bytes free (minimum " + MIN_FREE_STORAGE_BYTES + " bytes)", metrics);
        }

        return new DeviceHealthReport(deviceSerial, true, "Device is healthy", metrics);
    }
}
