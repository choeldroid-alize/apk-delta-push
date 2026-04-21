package com.apkdeltapush.dependency;

import java.util.Collections;
import java.util.List;

/**
 * Immutable result of a dependency compatibility check performed by
 * {@link DependencyChecker}.
 */
public final class DependencyCheckResult {

    private final boolean satisfied;
    private final List<String> missingDependencies;
    private final List<String> incompatibleDependencies;
    private final List<String> resolvedVersions;

    private DependencyCheckResult(
            boolean satisfied,
            List<String> missingDependencies,
            List<String> incompatibleDependencies,
            List<String> resolvedVersions) {
        this.satisfied = satisfied;
        this.missingDependencies = Collections.unmodifiableList(missingDependencies);
        this.incompatibleDependencies = Collections.unmodifiableList(incompatibleDependencies);
        this.resolvedVersions = Collections.unmodifiableList(resolvedVersions);
    }

    public static DependencyCheckResult satisfied(List<String> resolvedVersions) {
        return new DependencyCheckResult(true, Collections.emptyList(), Collections.emptyList(), resolvedVersions);
    }

    public static DependencyCheckResult unsatisfied(
            List<String> missing, List<String> incompatible) {
        return new DependencyCheckResult(false, missing, incompatible, Collections.emptyList());
    }

    public boolean isSatisfied() {
        return satisfied;
    }

    public List<String> getMissingDependencies() {
        return missingDependencies;
    }

    public List<String> getIncompatibleDependencies() {
        return incompatibleDependencies;
    }

    public List<String> getResolvedVersions() {
        return resolvedVersions;
    }

    public String getSummary() {
        if (satisfied) {
            return "All dependencies satisfied (" + resolvedVersions.size() + " resolved)";
        }
        StringBuilder sb = new StringBuilder("Dependency check failed.");
        if (!missingDependencies.isEmpty()) {
            sb.append(" Missing: ").append(missingDependencies);
        }
        if (!incompatibleDependencies.isEmpty()) {
            sb.append(" Incompatible: ").append(incompatibleDependencies);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "DependencyCheckResult{satisfied=" + satisfied
                + ", missing=" + missingDependencies
                + ", incompatible=" + incompatibleDependencies + "}";
    }
}
