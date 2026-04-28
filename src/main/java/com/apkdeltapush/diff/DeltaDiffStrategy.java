package com.apkdeltapush.diff;

/**
 * Defines the strategy used for computing APK deltas.
 */
public enum DeltaDiffStrategy {

    /**
     * Binary diff using bsdiff algorithm — best compression, slower.
     */
    BSDIFF,

    /**
     * Zip-aware diff that operates on individual zip entries — fast and
     * produces small deltas for APKs with many unchanged resources.
     */
    ZIP_ENTRY,

    /**
     * Simple byte-level XOR diff — very fast, larger output, useful for
     * near-identical APKs (e.g. signing-only changes).
     */
    XOR,

    /**
     * Automatically selects the best strategy based on APK size and
     * estimated similarity ratio.
     */
    AUTO;

    /**
     * Returns true if this strategy supports streaming (i.e. does not require
     * the full delta to be buffered in memory before writing).
     */
    public boolean supportsStreaming() {
        return this == XOR || this == ZIP_ENTRY;
    }

    /**
     * Returns true if this strategy is a concrete, resolvable strategy (i.e.
     * not {@code AUTO}).
     */
    public boolean isConcrete() {
        return this != AUTO;
    }

    /**
     * Returns the recommended strategy for a given APK size in bytes.
     *
     * @param apkSizeBytes size of the base APK
     * @return recommended strategy
     */
    public static DeltaDiffStrategy recommended(long apkSizeBytes) {
        if (apkSizeBytes < 0) {
            throw new IllegalArgumentException("apkSizeBytes must be non-negative, got: " + apkSizeBytes);
        }
        if (apkSizeBytes < 5 * 1024 * 1024) {
            return BSDIFF;
        } else if (apkSizeBytes < 50 * 1024 * 1024) {
            return ZIP_ENTRY;
        } else {
            return XOR;
        }
    }

    /**
     * Resolves {@code AUTO} to a concrete strategy based on the given APK size,
     * returning the strategy itself for all other values.
     *
     * @param apkSizeBytes size of the base APK in bytes
     * @return a concrete (non-{@code AUTO}) strategy
     */
    public DeltaDiffStrategy resolve(long apkSizeBytes) {
        if (this == AUTO) {
            return recommended(apkSizeBytes);
        }
        return this;
    }
}
