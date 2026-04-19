package com.apkdeltapush.fingerprint;

import java.util.Objects;

/**
 * Represents a unique fingerprint for a connected Android device,
 * combining device serial, ABI, SDK version, and installed APK digest.
 */
public class DeviceFingerprint {

    private final String deviceSerial;
    private final String abi;
    private final int sdkVersion;
    private final String installedApkDigest;

    public DeviceFingerprint(String deviceSerial, String abi, int sdkVersion, String installedApkDigest) {
        if (deviceSerial == null || deviceSerial.isBlank()) {
            throw new IllegalArgumentException("deviceSerial must not be blank");
        }
        if (abi == null || abi.isBlank()) {
            throw new IllegalArgumentException("abi must not be blank");
        }
        if (sdkVersion <= 0) {
            throw new IllegalArgumentException("sdkVersion must be positive");
        }
        this.deviceSerial = deviceSerial;
        this.abi = abi;
        this.sdkVersion = sdkVersion;
        this.installedApkDigest = installedApkDigest != null ? installedApkDigest : "";
    }

    public String getDeviceSerial() { return deviceSerial; }
    public String getAbi() { return abi; }
    public int getSdkVersion() { return sdkVersion; }
    public String getInstalledApkDigest() { return installedApkDigest; }

    /** Returns a compact string key suitable for cache lookups. */
    public String toCacheKey() {
        return deviceSerial + "|" + abi + "|" + sdkVersion + "|" + installedApkDigest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceFingerprint)) return false;
        DeviceFingerprint that = (DeviceFingerprint) o;
        return sdkVersion == that.sdkVersion
                && Objects.equals(deviceSerial, that.deviceSerial)
                && Objects.equals(abi, that.abi)
                && Objects.equals(installedApkDigest, that.installedApkDigest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSerial, abi, sdkVersion, installedApkDigest);
    }

    @Override
    public String toString() {
        return "DeviceFingerprint{serial='" + deviceSerial + "', abi='" + abi +
                "', sdk=" + sdkVersion + ", digest='" + installedApkDigest + "'}";
    }
}
