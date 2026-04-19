package com.apkdeltapush.snapshot;

/**
 * Immutable record of an APK installation state on a specific device.
 */
public class DeviceSnapshot {

    private final String deviceSerial;
    private final String packageName;
    private final String versionCode;
    private final String checksum;
    private final long capturedAtMs;

    public DeviceSnapshot(String deviceSerial, String packageName, String versionCode,
                          String checksum, long capturedAtMs) {
        this.deviceSerial = deviceSerial;
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.checksum = checksum;
        this.capturedAtMs = capturedAtMs;
    }

    public String getDeviceSerial() { return deviceSerial; }
    public String getPackageName() { return packageName; }
    public String getVersionCode() { return versionCode; }
    public String getChecksum() { return checksum; }
    public long getCapturedAtMs() { return capturedAtMs; }

    @Override
    public String toString() {
        return "DeviceSnapshot{device='" + deviceSerial + "', package='" + packageName +
                "', version='" + versionCode + "', checksum='" + checksum + "'}";
    }
}
