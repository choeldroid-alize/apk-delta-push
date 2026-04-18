package com.apkdeltapush.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to retrieve installed APK version info from a connected Android device via ADB.
 */
public class ApkVersionChecker {

    private static final Pattern VERSION_CODE_PATTERN =
            Pattern.compile("versionCode=(\\d+)");
    private static final Pattern VERSION_NAME_PATTERN =
            Pattern.compile("versionName=([\\w.\\-]+)");

    private final String adbPath;

    public ApkVersionChecker(String adbPath) {
        this.adbPath = adbPath;
    }

    /**
     * Returns the installed version code of the given package on the device,
     * or -1 if the package is not found.
     */
    public int getInstalledVersionCode(String deviceSerial, String packageName) throws IOException {
        String output = runAdbShell(deviceSerial, "dumpsys package " + packageName);
        Matcher m = VERSION_CODE_PATTERN.matcher(output);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return -1;
    }

    /**
     * Returns the installed version name of the given package on the device,
     * or null if the package is not found.
     */
    public String getInstalledVersionName(String deviceSerial, String packageName) throws IOException {
        String output = runAdbShell(deviceSerial, "dumpsys package " + packageName);
        Matcher m = VERSION_NAME_PATTERN.matcher(output);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private String runAdbShell(String deviceSerial, String command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                adbPath, "-s", deviceSerial, "shell", command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
