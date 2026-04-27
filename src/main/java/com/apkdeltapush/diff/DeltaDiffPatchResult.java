package com.apkdeltapush.diff;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Immutable result returned by {@link DeltaDiffPatcher#patch} describing the
 * outcome of applying a delta patch to a base APK.
 */
public class DeltaDiffPatchResult {

    private final Path outputPath;
    private final long outputSizeBytes;
    private final long elapsedMillis;
    private final boolean success;
    private final String errorMessage;

    public DeltaDiffPatchResult(Path outputPath, long outputSizeBytes, long elapsedMillis, boolean success) {
        this(outputPath, outputSizeBytes, elapsedMillis, success, null);
    }

    public DeltaDiffPatchResult(Path outputPath, long outputSizeBytes, long elapsedMillis,
                                boolean success, String errorMessage) {
        this.outputPath = Objects.requireNonNull(outputPath, "outputPath must not be null");
        this.outputSizeBytes = outputSizeBytes;
        this.elapsedMillis = elapsedMillis;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public long getOutputSizeBytes() {
        return outputSizeBytes;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "DeltaDiffPatchResult{" +
               "outputPath=" + outputPath +
               ", outputSizeBytes=" + outputSizeBytes +
               ", elapsedMillis=" + elapsedMillis +
               ", success=" + success +
               ", errorMessage='" + errorMessage + '\'' +
               '}';
    }
}
