package com.apkdeltapush.validation;

import java.util.Collections;
import java.util.List;

/**
 * Immutable result object returned by {@link PushPreflightValidator}.
 */
public class PreflightResult {

    private final boolean passed;
    private final List<String> violations;

    public PreflightResult(boolean passed, List<String> violations) {
        this.passed = passed;
        this.violations = violations != null ? violations : Collections.emptyList();
    }

    /** @return true if all pre-flight checks passed */
    public boolean isPassed() {
        return passed;
    }

    /** @return unmodifiable list of violation messages; empty when passed */
    public List<String> getViolations() {
        return violations;
    }

    /**
     * Returns a human-readable summary of the preflight result.
     */
    public String getSummary() {
        if (passed) {
            return "Pre-flight validation passed. No issues found.";
        }
        StringBuilder sb = new StringBuilder("Pre-flight validation FAILED with ");
        sb.append(violations.size()).append(" violation(s):\n");
        for (int i = 0; i < violations.size(); i++) {
            sb.append("  ").append(i + 1).append(". ").append(violations.get(i)).append("\n");
        }
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return "PreflightResult{passed=" + passed + ", violations=" + violations + "}";
    }
}
