package com.apkdeltapush.snapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages snapshots of installed APK states on devices,
 * enabling delta computation against a known baseline.
 */
public class DeviceSnapshotManager {

    private final Map<String, DeviceSnapshot> snapshots = new HashMap<>();

    /**
     * Records a snapshot for a given device and package.
     */
    public void recordSnapshot(String deviceSerial, String packageName, String versionCode, String checksum) {
        String key = buildKey(deviceSerial, packageName);
        DeviceSnapshot snapshot = new DeviceSnapshot(deviceSerial, packageName, versionCode, checksum,
                System.currentTimeMillis());
        snapshots.put(key, snapshot);
    }

    /**
     * Retrieves the latest snapshot for a device/package pair.
     */
    public Optional<DeviceSnapshot> getSnapshot(String deviceSerial, String packageName) {
        return Optional.ofNullable(snapshots.get(buildKey(deviceSerial, packageName)));
    }

    /**
     * Returns true if a snapshot exists and the checksum differs from the provided one.
     */
    public boolean isStale(String deviceSerial, String packageName, String currentChecksum) {
        return getSnapshot(deviceSerial, packageName)
                .map(s -> !s.getChecksum().equals(currentChecksum))
                .orElse(false);
    }

    /**
     * Removes a snapshot entry.
     */
    public boolean removeSnapshot(String deviceSerial, String packageName) {
        return snapshots.remove(buildKey(deviceSerial, packageName)) != null;
    }

    /**
     * Returns the total number of tracked snapshots.
     */
    public int snapshotCount() {
        return snapshots.size();
    }

    private String buildKey(String deviceSerial, String packageName) {
        return deviceSerial + ":" + packageName;
    }
}
