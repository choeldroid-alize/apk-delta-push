package com.apkdeltapush.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Validates a {@link DeltaDiffResult} before it is applied or cached,
 * ensuring the diff is internally consistent and meets minimum quality
 * thresholds defined in {@link DeltaDiffOptions}.
 */
public class DeltaDiffValidator {

    private static final double MAX_ACCEPTABLE_RATIO = 1.05; // delta must not exceed 105 % of source
    private static final int MIN_SOURCE_SIZE_BYTES = 1;

    public static final class ValidationOutcome {
        private final boolean valid;
        private final List<String> violations;

        private ValidationOutcome(boolean valid, List<String> violations) {
            this.valid = valid;
            this.violations = List.copyOf(violations);
        }

        public boolean isValid() { return valid; }
        public List<String> getViolations() { return violations; }

        @Override
        public String toString() {
            return valid ? "VALID" : "INVALID: " + violations;
        }
    }

    /**
     * Validates the given diff result against the supplied options.
     *
     * @param result  the diff result to validate; must not be null
     * @param options the options that governed diff generation; must not be null
     * @return a {@link ValidationOutcome} describing whether the result is acceptable
     */
    public ValidationOutcome validate(DeltaDiffResult result, DeltaDiffOptions options) {
        Objects.requireNonNull(result, "result must not be null");
        Objects.requireNonNull(options, "options must not be null");

        List<String> violations = new ArrayList<>();

        if (result.getSourceSize() < MIN_SOURCE_SIZE_BYTES) {
            violations.add("sourceSize must be >= " + MIN_SOURCE_SIZE_BYTES
                    + " but was " + result.getSourceSize());
        }

        if (result.getTargetSize() < MIN_SOURCE_SIZE_BYTES) {
            violations.add("targetSize must be >= " + MIN_SOURCE_SIZE_BYTES
                    + " but was " + result.getTargetSize());
        }

        if (result.getDeltaSize() < 0) {
            violations.add("deltaSize must not be negative but was " + result.getDeltaSize());
        }

        if (result.getSourceSize() > 0) {
            double ratio = (double) result.getDeltaSize() / result.getSourceSize();
            if (ratio > MAX_ACCEPTABLE_RATIO) {
                violations.add(String.format(
                        "delta/source ratio %.3f exceeds maximum %.3f", ratio, MAX_ACCEPTABLE_RATIO));
            }
        }

        if (result.getChecksum() == null || result.getChecksum().isBlank()) {
            violations.add("checksum must not be null or blank");
        }

        if (options.isRequireMetadata() && result.getMetadata() == null) {
            violations.add("metadata is required by options but was null");
        }

        return new ValidationOutcome(violations.isEmpty(), violations);
    }
}
