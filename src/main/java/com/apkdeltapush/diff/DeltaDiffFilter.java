package com.apkdeltapush.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Filters delta diff results based on configurable criteria such as
 * minimum size threshold, file extension, or change ratio.
 */
public class DeltaDiffFilter {

    private final List<Predicate<DeltaDiffResult>> predicates = new ArrayList<>();
    private long minDeltaSizeBytes = 0L;
    private double maxChangeRatio = 1.0;
    private List<String> excludedExtensions = new ArrayList<>();

    public DeltaDiffFilter withMinDeltaSize(long bytes) {
        if (bytes < 0) throw new IllegalArgumentException("minDeltaSizeBytes must be >= 0");
        this.minDeltaSizeBytes = bytes;
        return this;
    }

    public DeltaDiffFilter withMaxChangeRatio(double ratio) {
        if (ratio < 0.0 || ratio > 1.0) throw new IllegalArgumentException("changeRatio must be in [0.0, 1.0]");
        this.maxChangeRatio = ratio;
        return this;
    }

    public DeltaDiffFilter withExcludedExtensions(List<String> extensions) {
        if (extensions != null) {
            this.excludedExtensions = new ArrayList<>(extensions);
        }
        return this;
    }

    public DeltaDiffFilter withCustomPredicate(Predicate<DeltaDiffResult> predicate) {
        if (predicate != null) {
            this.predicates.add(predicate);
        }
        return this;
    }

    /**
     * Returns true if the given result passes all active filter criteria.
     */
    public boolean accepts(DeltaDiffResult result) {
        if (result == null) return false;

        if (result.getDeltaSize() < minDeltaSizeBytes) {
            return false;
        }

        long originalSize = result.getOriginalSize();
        if (originalSize > 0) {
            double ratio = (double) result.getDeltaSize() / originalSize;
            if (ratio > maxChangeRatio) {
                return false;
            }
        }

        String path = result.getFilePath();
        if (path != null) {
            for (String ext : excludedExtensions) {
                if (path.endsWith(ext)) {
                    return false;
                }
            }
        }

        for (Predicate<DeltaDiffResult> predicate : predicates) {
            if (!predicate.test(result)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Filters a list of diff results, returning only those that pass all criteria.
     */
    public List<DeltaDiffResult> filter(List<DeltaDiffResult> results) {
        if (results == null) return new ArrayList<>();
        List<DeltaDiffResult> filtered = new ArrayList<>();
        for (DeltaDiffResult r : results) {
            if (accepts(r)) filtered.add(r);
        }
        return filtered;
    }
}
