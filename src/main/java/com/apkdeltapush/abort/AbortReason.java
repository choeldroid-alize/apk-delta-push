package com.apkdeltapush.abort;

/**
 * Represents the reason why a push operation was aborted.
 */
public enum AbortReason {
    USER_REQUESTED("Aborted by user request", false),
    DEVICE_DISCONNECTED("Target device disconnected", true),
    QUOTA_EXCEEDED("Transfer quota exceeded", false),
    CHECKSUM_MISMATCH("Patch checksum validation failed", true),
    NETWORK_FAILURE("Network condition degraded below threshold", true),
    TIMEOUT("Operation timed out", true),
    INTERNAL_ERROR("Internal error occurred", false);

    private final String description;
    private final boolean retryable;

    AbortReason(String description, boolean retryable) {
        this.description = description;
        this.retryable = retryable;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns whether the operation that triggered this abort reason
     * is a candidate for an automatic retry.
     *
     * @return {@code true} if the operation may be retried, {@code false} otherwise
     */
    public boolean isRetryable() {
        return retryable;
    }

    @Override
    public String toString() {
        return name() + ": " + description;
    }
}
