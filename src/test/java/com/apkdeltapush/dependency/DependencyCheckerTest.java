package com.apkdeltapush.dependency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DependencyCheckerTest {

    private DependencyChecker checker;

    @BeforeEach
    void setUp() {
        checker = new DependencyChecker();
    }

    @Test
    void checkReturnsSatisfiedWhenAllDependenciesMet() {
        Map<String, String> required = new HashMap<>();
        required.put("libssl", "1.1.0");
        required.put("libz", "1.2.11");

        Map<String, String> available = new HashMap<>();
        available.put("libssl", "1.1.1");
        available.put("libz", "1.2.11");

        DependencyCheckResult result = checker.check(required, available);

        assertTrue(result.isSatisfied());
        assertTrue(result.getMissingDependencies().isEmpty());
        assertTrue(result.getIncompatibleDependencies().isEmpty());
    }

    @Test
    void checkReturnsUnsatisfiedWhenDependencyMissing() {
        Map<String, String> required = new HashMap<>();
        required.put("libcrypto", "1.0.0");

        Map<String, String> available = new HashMap<>();

        DependencyCheckResult result = checker.check(required, available);

        assertFalse(result.isSatisfied());
        assertTrue(result.getMissingDependencies().contains("libcrypto"));
    }

    @Test
    void checkReturnsUnsatisfiedWhenVersionTooOld() {
        Map<String, String> required = new HashMap<>();
        required.put("libssl", "1.2.0");

        Map<String, String> available = new HashMap<>();
        available.put("libssl", "1.1.0");

        DependencyCheckResult result = checker.check(required, available);

        assertFalse(result.isSatisfied());
        assertFalse(result.getIncompatibleDependencies().isEmpty());
        assertTrue(result.getIncompatibleDependencies().get(0).contains("libssl"));
    }

    @Test
    void checkReturnsSatisfiedForEmptyRequirements() {
        DependencyCheckResult result = checker.check(new HashMap<>(), new HashMap<>());
        assertTrue(result.isSatisfied());
    }

    @Test
    void checkReturnsSatisfiedForNullRequirements() {
        DependencyCheckResult result = checker.check(null, new HashMap<>());
        assertTrue(result.isSatisfied());
    }

    @Test
    void resolvedDependenciesPopulatedAfterSuccessfulCheck() {
        Map<String, String> required = new HashMap<>();
        required.put("libssl", "1.0.0");

        Map<String, String> available = new HashMap<>();
        available.put("libssl", "1.1.0");

        checker.check(required, available);

        Map<String, String> resolved = checker.getResolvedDependencies();
        assertTrue(resolved.containsKey("libssl"));
        assertEquals("1.1.0", resolved.get("libssl"));
    }

    @Test
    void resetClearsResolvedDependencies() {
        Map<String, String> required = new HashMap<>();
        required.put("libssl", "1.0.0");
        Map<String, String> available = new HashMap<>();
        available.put("libssl", "1.0.0");
        checker.check(required, available);

        checker.reset();

        assertTrue(checker.getResolvedDependencies().isEmpty());
    }

    @Test
    void getSummaryDescribesSatisfiedResult() {
        DependencyCheckResult result = DependencyCheckResult.satisfied(java.util.List.of("1.1.0"));
        assertTrue(result.getSummary().contains("satisfied"));
    }

    @Test
    void getSummaryDescribesUnsatisfiedResult() {
        DependencyCheckResult result = DependencyCheckResult.unsatisfied(
                java.util.List.of("libmissing"), java.util.List.of());
        assertTrue(result.getSummary().contains("Missing"));
        assertTrue(result.getSummary().contains("libmissing"));
    }
}
