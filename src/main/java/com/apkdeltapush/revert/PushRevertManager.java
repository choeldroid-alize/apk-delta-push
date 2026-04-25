package com.apkdeltapush.revert;

import com.apkdeltapush.adb.AdbClient;
import com.apkdeltapush.rollback.RollbackManager;
import com.apkdeltapush.history.PushHistoryManager;
import com.apkdeltapush.history.PushHistoryRecord;
import com.apkdeltapush.verify.InstallVerifier;
import com.apkdeltapush.verify.VerificationResult;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Orchestrates reverting a device to a previously installed APK version
 * by combining history lookup, rollback execution, and post-revert verification.
 */
public class PushRevertManager {

    private static final Logger logger = Logger.getLogger(PushRevertManager.class.getName());

    private final AdbClient adbClient;
    private final RollbackManager rollbackManager;
    private final PushHistoryManager historyManager;
    private final InstallVerifier installVerifier;

    public PushRevertManager(AdbClient adbClient,
                             RollbackManager rollbackManager,
                             PushHistoryManager historyManager,
                             InstallVerifier installVerifier) {
        this.adbClient = Objects.requireNonNull(adbClient, "adbClient must not be null");
        this.rollbackManager = Objects.requireNonNull(rollbackManager, "rollbackManager must not be null");
        this.historyManager = Objects.requireNonNull(historyManager, "historyManager must not be null");
        this.installVerifier = Objects.requireNonNull(installVerifier, "installVerifier must not be null");
    }

    /**
     * Reverts the given package on the specified device to its last known good version.
     *
     * @param deviceSerial the ADB serial of the target device
     * @param packageName  the Android package name to revert
     * @return a {@link RevertResult} describing the outcome
     */
    public RevertResult revert(String deviceSerial, String packageName) {
        Objects.requireNonNull(deviceSerial, "deviceSerial must not be null");
        Objects.requireNonNull(packageName, "packageName must not be null");

        logger.info(String.format("Starting revert for package '%s' on device '%s'", packageName, deviceSerial));

        Optional<PushHistoryRecord> recordOpt = historyManager.findLastSuccessful(deviceSerial, packageName);
        if (recordOpt.isEmpty()) {
            logger.warning("No successful push history found; cannot revert.");
            return RevertResult.failure(deviceSerial, packageName, "No previous successful push record found");
        }

        PushHistoryRecord record = recordOpt.get();
        String targetVersion = record.getVersionName();
        String apkPath = record.getApkPath();

        logger.info(String.format("Rolling back to version '%s' using APK '%s'", targetVersion, apkPath));

        boolean rolled = rollbackManager.rollback(deviceSerial, packageName, apkPath);
        if (!rolled) {
            return RevertResult.failure(deviceSerial, packageName, "Rollback execution failed");
        }

        VerificationResult verification = installVerifier.verify(deviceSerial, packageName, targetVersion);
        if (!verification.isSuccess()) {
            logger.severe("Post-revert verification failed: " + verification.getMessage());
            return RevertResult.failure(deviceSerial, packageName,
                    "Rollback applied but verification failed: " + verification.getMessage());
        }

        logger.info("Revert completed successfully.");
        return RevertResult.success(deviceSerial, packageName, targetVersion);
    }
}
