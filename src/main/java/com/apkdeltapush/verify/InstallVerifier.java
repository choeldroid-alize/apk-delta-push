package com.apkdeltapush.verify;

import com.apkdeltapush.adb.AdbClient;
import java.util.logging.Logger;

/**
 * Verifies that an APK was successfully installed on the device
 * by checking the installed version code and package name.
 */
public class InstallVerifier {

    private static final Logger logger = Logger.getLogger(InstallVerifier.class.getName());

    private final AdbClient adbClient;

    public InstallVerifier(AdbClient adbClient) {
        if (adbClient == null) throw new IllegalArgumentException("AdbClient must not be null");
        this.adbClient = adbClient;
    }

    /**
     * Verifies that the given package is installed with at least the expected version code.
     *
     * @param deviceSerial   target device serial
     * @param packageName    APK package name
     * @param expectedVersion expected version code
     * @return VerificationResult with status and details
     */
    public VerificationResult verify(String deviceSerial, String packageName, int expectedVersion) {
        if (deviceSerial == null || packageName == null) {
            return VerificationResult.failure("deviceSerial and packageName must not be null");
        }
        try {
            String output = adbClient.runShellCommand(
                deviceSerial,
                "dumpsys package " + packageName + " | grep versionCode"
            );
            if (output == null || output.isBlank()) {
                return VerificationResult.failure("Package not found: " + packageName);
            }
            int installedVersion = parseVersionCode(output);
            if (installedVersion >= expectedVersion) {
                logger.info(String.format("Verified %s versionCode=%d on %s",
                    packageName, installedVersion, deviceSerial));
                return VerificationResult.success(installedVersion);
            } else {
                return VerificationResult.failure(String.format(
                    "Expected versionCode>=%d but found %d", expectedVersion, installedVersion));
            }
        } catch (Exception e) {
            logger.severe("Verification failed: " + e.getMessage());
            return VerificationResult.failure("ADB error: " + e.getMessage());
        }
    }

    private int parseVersionCode(String dumpsysOutput) {
        // Example line: "    versionCode=42 minSdk=21 targetSdk=33"
        for (String token : dumpsysOutput.split("\\s+")) {
            if (token.startsWith("versionCode=")) {
                String[] parts = token.split("=");
                if (parts.length >= 2) {
                    return Integer.parseInt(parts[1].trim());
                }
            }
        }
        throw new IllegalArgumentException("versionCode not found in: " + dumpsysOutput);
    }
}
