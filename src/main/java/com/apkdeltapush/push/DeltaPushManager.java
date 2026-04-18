package com.apkdeltapush.push;

import com.apkdeltapush.adb.AdbClient;
import com.apkdeltapush.diff.ApkDiffGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Orchestrates the delta push workflow:
 * 1. Generate a binary diff between old and new APK
 * 2. Push the patch to the device via ADB
 * 3. Trigger remote apply (or full install fallback)
 */
public class DeltaPushManager {

    private static final Logger LOG = Logger.getLogger(DeltaPushManager.class.getName());

    private final AdbClient adbClient;
    private final ApkDiffGenerator diffGenerator;

    public DeltaPushManager(AdbClient adbClient, ApkDiffGenerator diffGenerator) {
        this.adbClient = adbClient;
        this.diffGenerator = diffGenerator;
    }

    /**
     * Performs a delta push of {@code newApk} to the connected device identified
     * by {@code deviceSerial}, using {@code oldApk} as the base for diffing.
     *
     * @return {@link PushResult} describing success/failure and bytes transferred
     */
    public PushResult push(String deviceSerial, File oldApk, File newApk) throws IOException {
        if (!oldApk.exists() || !newApk.exists()) {
            throw new IllegalArgumentException("Both oldApk and newApk must exist on disk.");
        }

        Path patchFile = Files.createTempFile("apk-delta-", ".patch");
        try {
            LOG.info("Generating diff: " + oldApk.getName() + " -> " + newApk.getName());
            diffGenerator.generate(oldApk, newApk, patchFile.toFile());

            long patchSize = Files.size(patchFile);
            long newApkSize = Files.size(newApk.toPath());
            LOG.info(String.format("Patch size: %d bytes (%.1f%% of full APK)",
                    patchSize, patchSize * 100.0 / newApkSize));

            // If patch is larger than the APK itself, fall back to full install
            if (patchSize >= newApkSize) {
                LOG.warning("Patch larger than APK — falling back to full install.");
                adbClient.installApk(deviceSerial, newApk);
                return new PushResult(false, newApkSize);
            }

            String remotePatchPath = "/data/local/tmp/" + patchFile.getFileName();
            adbClient.pushFile(deviceSerial, patchFile.toFile(), remotePatchPath);
            adbClient.applyPatch(deviceSerial, remotePatchPath);

            return new PushResult(true, patchSize);
        } finally {
            Files.deleteIfExists(patchFile);
        }
    }
}
