package com.apkdeltapush.abort;

/**
 * Represents the reason why a push operation was aborted.
 */
public enum AbortReason {
    USER_REQUESTED("Aborted by user request"),
    DEVICE_DISCONNECTED("Target device disconnected"),
    QUOTA_EXCEEDED("Transfer quota exceeded"),
    CHECKSUM_MISMATCH("Patch checksum validation failed"),
    NETWORK_FAILURE("Network condition degraded below threshold"),
    TIMEOUT("Operation timed out"),
    INTERNAL_ERROR("Internal error occurred");

    private final String description;

    AbortReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name() + ": " + description;
    }
}
