package com.apkdeltapush.progress;

/**
 * Represents the phases of an APK delta push operation.
 */
public enum PushPhase {
    INITIALIZING("Initializing"),
    DIFFING("Generating diff"),
    VALIDATING("Validating patch"),
    TRANSFERRING("Transferring patch"),
    APPLYING("Applying patch"),
    COMPLETE("Complete");

    private final String displayName;

    PushPhase(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
