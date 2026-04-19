package com.apkdeltapush.verify;

/**
 * Immutable result of an APK install verification.
 */
public final class VerificationResult {

    private final boolean success;
    private final int installedVersionCode;
    private final String errorMessage;

    private VerificationResult(boolean success, int installedVersionCode, String errorMessage) {
        this.success = success;
        this.installedVersionCode = installedVersionCode;
        this.errorMessage = errorMessage;
    }

    public static VerificationResult success(int installedVersionCode) {
        return new VerificationResult(true, installedVersionCode, null);
    }

    public static VerificationResult failure(String errorMessage) {
        return new VerificationResult(false, -1, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getInstalledVersionCode() {
        return installedVersionCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        if (success) {
            return "VerificationResult{success=true, versionCode=" + installedVersionCode + "}";
        }
        return "VerificationResult{success=false, error='" + errorMessage + "'}";
    }
}
