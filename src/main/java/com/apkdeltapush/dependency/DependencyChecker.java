package com.apkdeltapush.dependency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Checks and validates APK dependencies before applying a delta push.
 * Ensures that required shared libraries, split APKs, and version constraints
 * are satisfied on the target device before proceeding with installation.
 */
public class DependencyChecker {

    private final Map<String, String> resolvedDependencies = new ConcurrentHashMap<>();

    /**
     * Checks whether all declared dependencies in the given manifest are
     * satisfied by the provided device dependency map.
     *
     * @param required  map of dependency name to minimum required version
     * @param available map of dependency name to version present on device
     * @return a {@link DependencyCheckResult} describing the outcome
     */
    public DependencyCheckResult check(Map<String, String> required, Map<String, String> available) {
        if (required == null || required.isEmpty()) {
            return DependencyCheckResult.satisfied(Collections.emptyList());
        }

        List<String> missing = new ArrayList<>();
        List<String> incompatible = new ArrayList<>();

        for (Map.Entry<String, String> entry : required.entrySet()) {
            String name = entry.getKey();
            String requiredVersion = entry.getValue();

            if (!available.containsKey(name)) {
                missing.add(name);
                continue;
            }

            String deviceVersion = available.get(name);
            if (!isVersionCompatible(requiredVersion, deviceVersion)) {
                incompatible.add(name + " (required >= " + requiredVersion + ", found " + deviceVersion + ")");
            } else {
                resolvedDependencies.put(name, deviceVersion);
            }
        }

        if (!missing.isEmpty() || !incompatible.isEmpty()) {
            return DependencyCheckResult.unsatisfied(missing, incompatible);
        }

        return DependencyCheckResult.satisfied(new ArrayList<>(resolvedDependencies.values()));
    }

    /**
     * Returns a snapshot of all successfully resolved dependency versions
     * from the last {@link #check} invocation.
     */
    public Map<String, String> getResolvedDependencies() {
        return Collections.unmodifiableMap(resolvedDependencies);
    }

    /** Clears the resolved dependency cache. */
    public void reset() {
        resolvedDependencies.clear();
    }

    private boolean isVersionCompatible(String required, String available) {
        try {
            int[] req = parseVersion(required);
            int[] avail = parseVersion(available);
            for (int i = 0; i < Math.min(req.length, avail.length); i++) {
                if (avail[i] > req[i]) return true;
                if (avail[i] < req[i]) return false;
            }
            return avail.length >= req.length;
        } catch (NumberFormatException e) {
            return available.compareTo(required) >= 0;
        }
    }

    private int[] parseVersion(String version) {
        String[] parts = version.split("\\.");
        int[] nums = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            nums[i] = Integer.parseInt(parts[i].trim());
        }
        return nums;
    }
}
