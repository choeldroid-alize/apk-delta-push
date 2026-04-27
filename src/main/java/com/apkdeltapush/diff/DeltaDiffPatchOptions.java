package com.apkdeltapush.diff;

/**
 * Configuration options controlling the behaviour of {@link DeltaDiffPatcher}.
 */
public class DeltaDiffPatchOptions {

    private final boolean validateOutputSize;
    private final boolean overwriteExisting;
    private final int bufferSizeBytes;

    private DeltaDiffPatchOptions(Builder builder) {
        this.validateOutputSize = builder.validateOutputSize;
        this.overwriteExisting = builder.overwriteExisting;
        this.bufferSizeBytes = builder.bufferSizeBytes;
    }

    public boolean isValidateOutputSize() {
        return validateOutputSize;
    }

    public boolean isOverwriteExisting() {
        return overwriteExisting;
    }

    public int getBufferSizeBytes() {
        return bufferSizeBytes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static DeltaDiffPatchOptions defaults() {
        return builder().build();
    }

    @Override
    public String toString() {
        return "DeltaDiffPatchOptions{" +
               "validateOutputSize=" + validateOutputSize +
               ", overwriteExisting=" + overwriteExisting +
               ", bufferSizeBytes=" + bufferSizeBytes +
               '}';
    }

    public static class Builder {
        private boolean validateOutputSize = true;
        private boolean overwriteExisting = false;
        private int bufferSizeBytes = 8192;

        public Builder validateOutputSize(boolean validateOutputSize) {
            this.validateOutputSize = validateOutputSize;
            return this;
        }

        public Builder overwriteExisting(boolean overwriteExisting) {
            this.overwriteExisting = overwriteExisting;
            return this;
        }

        public Builder bufferSizeBytes(int bufferSizeBytes) {
            if (bufferSizeBytes <= 0) throw new IllegalArgumentException("bufferSizeBytes must be positive");
            this.bufferSizeBytes = bufferSizeBytes;
            return this;
        }

        public DeltaDiffPatchOptions build() {
            return new DeltaDiffPatchOptions(this);
        }
    }
}
