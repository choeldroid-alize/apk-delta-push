package com.apkdeltapush.metadata;

import java.util.Objects;

/**
 * Holds metadata extracted from an APK file for use in diff and push operations.
 */
public class ApkMetadata {

    private final String packageName;
    private final int versionCode;
    private final String versionName;
    private final long fileSizeBytes;
    private final String sha256Checksum;
    private final String minSdkVersion;
    private final String targetSdkVersion;

    public ApkMetadata(String packageName, int versionCode, String versionName,
                       long fileSizeBytes, String sha256Checksum,
                       String minSdkVersion, String targetSdkVersion) {
        this.packageName = Objects.requireNonNull(packageName, "packageName must not be null");
        this.versionCode = versionCode;
        this.versionName = Objects.requireNonNull(versionName, "versionName must not be null");
        this.fileSizeBytes = fileSizeBytes;
        this.sha256Checksum = Objects.requireNonNull(sha256Checksum, "sha256Checksum must not be null");
        this.minSdkVersion = minSdkVersion;
        this.targetSdkVersion = targetSdkVersion;
    }

    public String getPackageName() { return packageName; }
    public int getVersionCode() { return versionCode; }
    public String getVersionName() { return versionName; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public String getSha256Checksum() { return sha256Checksum; }
    public String getMinSdkVersion() { return minSdkVersion; }
    public String getTargetSdkVersion() { return targetSdkVersion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApkMetadata)) return false;
        ApkMetadata that = (ApkMetadata) o;
        return versionCode == that.versionCode &&
               fileSizeBytes == that.fileSizeBytes &&
               Objects.equals(packageName, that.packageName) &&
               Objects.equals(sha256Checksum, that.sha256Checksum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, versionCode, sha256Checksum);
    }

    @Override
    public String toString() {
        return "ApkMetadata{" +
               "packageName='" + packageName + '\'' +
               ", versionCode=" + versionCode +
               ", versionName='" + versionName + '\'' +
               ", fileSizeBytes=" + fileSizeBytes +
               ", sha256='" + sha256Checksum + '\'' +
               '}';
    }
}
