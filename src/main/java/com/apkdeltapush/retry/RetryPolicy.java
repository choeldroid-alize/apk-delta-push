package com.apkdeltapush.retry;

import java.util.logging.Logger;

/**
 * Defines retry behavior for failed ADB push operations.
 */
public class RetryPolicy {

    private static final Logger logger = Logger.getLogger(RetryPolicy.class.getName());

    private final int maxAttempts;
    private final long initialDelayMs;
    private final double backoffMultiplier;

    public RetryPolicy(int maxAttempts, long initialDelayMs, double backoffMultiplier) {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
        if (initialDelayMs < 0) throw new IllegalArgumentException("initialDelayMs must be >= 0");
        if (backoffMultiplier < 1.0) throw new IllegalArgumentException("backoffMultiplier must be >= 1.0");
        this.maxAttempts = maxAttempts;
        this.initialDelayMs = initialDelayMs;
        this.backoffMultiplier = backoffMultiplier;
    }

    public static RetryPolicy defaultPolicy() {
        return new RetryPolicy(3, 500, 2.0);
    }

    public static RetryPolicy noRetry() {
        return new RetryPolicy(1, 0, 1.0);
    }

    public <T> T execute(RetryableOperation<T> operation) throws Exception {
        int attempt = 0;
        long delay = initialDelayMs;
        Exception lastException = null;

        while (attempt < maxAttempts) {
            attempt++;
            try {
                logger.fine("Attempt " + attempt + " of " + maxAttempts);
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                logger.warning("Attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                    delay = (long) (delay * backoffMultiplier);
                }
            }
        }
        throw new RetryExhaustedException("All " + maxAttempts + " attempts failed", lastException);
    }

    public int getMaxAttempts() { return maxAttempts; }
    public long getInitialDelayMs() { return initialDelayMs; }
    public double getBackoffMultiplier() { return backoffMultiplier; }
}
