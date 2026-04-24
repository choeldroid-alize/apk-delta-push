package com.apkdeltapush.validation;

import com.apkdeltapush.adb.AdbClient;
import com.apkdeltapush.signature.ApkSignatureValidator;
import com.apkdeltapush.util.ApkVersionChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Performs pre-flight validation checks before initiating an APK delta push.
 * Aggregates results from signature, version, and device connectivity checks.
 */
public class PushPreflightValidator {

    private final AdbClient adbClient;
    private final ApkSignatureValidator signatureValidator;
    private final ApkVersionChecker versionChecker;

    public PushPreflightValidator(AdbClient adbClient,
                                  ApkSignatureValidator signatureValidator,
                                  ApkVersionChecker versionChecker) {
        this.adbClient = adbClient;
        this.signatureValidator = signatureValidator;
        this.versionChecker = versionChecker;
    }

    /**
     * Runs all pre-flight checks for the given device and APK file.
     *
     * @param deviceSerial the ADB device serial
     * @param apkFile      the APK file to be pushed
     * @return a {@link PreflightResult} containing pass/fail status and any violations
     */
    public PreflightResult validate(String deviceSerial, File apkFile) {
        List<String> violations = new ArrayList<>();

        if (deviceSerial == null || deviceSerial.isBlank()) {
            violations.add("Device serial must not be null or blank.");
        } else if (!adbClient.isDeviceConnected(deviceSerial)) {
            violations.add("Device '" + deviceSerial + "' is not connected via ADB.");
        }

        if (apkFile == null || !apkFile.exists()) {
            violations.add("APK file does not exist: " + (apkFile != null ? apkFile.getPath() : "null"));
        } else {
            if (!apkFile.getName().endsWith(".apk")) {
                violations.add("File does not have .apk extension: " + apkFile.getName());
            }
            boolean signatureValid = signatureValidator.validate(apkFile);
            if (!signatureValid) {
                violations.add("APK signature validation failed for: " + apkFile.getName());
            }
            if (!violations.isEmpty()) {
                return new PreflightResult(false, violations);
            }
            boolean isNewer = versionChecker.isNewerThanInstalled(deviceSerial, apkFile);
            if (!isNewer) {
                violations.add("APK version is not newer than the installed version on device '" + deviceSerial + "'.");
            }
        }

        boolean passed = violations.isEmpty();
        return new PreflightResult(passed, Collections.unmodifiableList(violations));
    }
}
