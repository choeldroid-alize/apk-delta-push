package com.apkdeltapush.quota;

/**
 * Thrown when a transfer quota limit is exceeded during a push session.
 */
public class QuotaExceededException extends Exception {

    private final long bytesAttempted;
    private final long quotaLimit;

    public QuotaExceededException(String message) {
        super(message);
        this.bytesAttempted = -1;
        this.quotaLimit = -1;
    }

    public QuotaExceededException(String message, long bytesAttempted, long quotaLimit) {
        super(message);
        this.bytesAttempted = bytesAttempted;
        this.quotaLimit = quotaLimit;
    }

    public long getBytesAttempted() {
        return bytesAttempted;
    }

    public long getQuotaLimit() {
        return quotaLimit;
    }
}
