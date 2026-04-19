package com.apkdeltapush.rollback;

import com.apkdeltapush.adb.AdbClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Manages APK rollback capability by storing pre-push snapshots
 * and restoring them on failure or explicit rollback request.
 */
public class RollbackManager {

    private static final Logger LOG = Logger.getLogger(RollbackManager.class.getName());
    private static final String DEVICE_BACKUP_PATH = "/data/local/tmp/apkdeltapush/backup/";

    private final AdbClient adbClient;
    private final Map<String, RollbackEntry> rollbackRegistry = new HashMap<>();

    public RollbackManager(AdbClient adbClient) {
        this.adbClient = adbClient;
    }

    /**
     * Creates a backup of the currently installed APK on the device before pushing a delta.
     *
     * @param deviceSerial  target device serial
     * @param packageName   package to back up
     * @param apkDevicePath current APK path on device (from pm path)
     * @return true if backup succeeded
     */
    public boolean createBackup(String deviceSerial, String packageName, String apkDevicePath) {
        String backupDest = DEVICE_BACKUP_PATH + packageName + ".bak.apk";
        try {
            adbClient.executeShellCommand(deviceSerial, "mkdir -p " + DEVICE_BACKUP_PATH);
            adbClient.executeShellCommand(deviceSerial, "cp " + apkDevicePath + " " + backupDest);
            rollbackRegistry.put(key(deviceSerial, packageName),
                    new RollbackEntry(deviceSerial, packageName, backupDest, apkDevicePath));
            LOG.info("Backup created for " + packageName + " on " + deviceSerial);
            return true;
        } catch (IOException e) {
            LOG.warning("Failed to create backup: " + e.getMessage());
            return false;
        }
    }

    /**
     * Rolls back the APK on the device to the backed-up version.
     *
     * @param deviceSerial target device serial
     * @param packageName  package to roll back
     * @return true if rollback succeeded
     */
    public boolean rollback(String deviceSerial, String packageName) {
        Optional<RollbackEntry> entry = getEntry(deviceSerial, packageName);
        if (entry.isEmpty()) {
            LOG.warning("No rollback entry found for " + packageName + " on " + deviceSerial);
            return false;
        }
        RollbackEntry e = entry.get();
        try {
            adbClient.executeShellCommand(deviceSerial,
                    "pm install -r " + e.getBackupPath());
            LOG.info("Rolled back " + packageName + " on " + deviceSerial);
            clearBackup(deviceSerial, packageName);
            return true;
        } catch (IOException ex) {
            LOG.severe("Rollback failed: " + ex.getMessage());
            return false;
        }
    }

    public boolean hasBackup(String deviceSerial, String packageName) {
        return rollbackRegistry.containsKey(key(deviceSerial, packageName));
    }

    public void clearBackup(String deviceSerial, String packageName) {
        RollbackEntry e = rollbackRegistry.remove(key(deviceSerial, packageName));
        if (e != null) {
            try {
                adbClient.executeShellCommand(deviceSerial, "rm -f " + e.getBackupPath());
            } catch (IOException ex) {
                LOG.warning("Could not remove backup file: " + ex.getMessage());
            }
        }
    }

    private Optional<RollbackEntry> getEntry(String deviceSerial, String packageName) {
        return Optional.ofNullable(rollbackRegistry.get(key(deviceSerial, packageName)));
    }

    private String key(String deviceSerial, String packageName) {
        return deviceSerial + ":" + packageName;
    }
}
