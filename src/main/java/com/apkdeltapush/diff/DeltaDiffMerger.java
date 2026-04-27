package com.apkdeltapush.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Merges multiple DeltaDiffResult instances into a single consolidated result,
 * useful when combining partial diffs from fragmented or multi-source APK deltas.
 */
public class DeltaDiffMerger {

    private static final Logger logger = Logger.getLogger(DeltaDiffMerger.class.getName());

    public static final int MAX_MERGE_INPUTS = 32;

    /**
     * Merges a list of DeltaDiffResult objects into one combined result.
     *
     * @param results non-null, non-empty list of results to merge
     * @return a merged DeltaDiffResult
     * @throws IllegalArgumentException if results list is null, empty, or exceeds MAX_MERGE_INPUTS
     */
    public DeltaDiffResult merge(List<DeltaDiffResult> results) {
        Objects.requireNonNull(results, "results must not be null");
        if (results.isEmpty()) {
            throw new IllegalArgumentException("Cannot merge an empty list of DeltaDiffResults");
        }
        if (results.size() > MAX_MERGE_INPUTS) {
            throw new IllegalArgumentException(
                "Merge input count " + results.size() + " exceeds maximum of " + MAX_MERGE_INPUTS);
        }

        logger.fine("Merging " + results.size() + " DeltaDiffResult(s)");

        long totalOriginalSize = 0;
        long totalPatchedSize = 0;
        long totalDeltaSize = 0;
        boolean anyFailed = false;
        List<String> mergedWarnings = new ArrayList<>();

        for (DeltaDiffResult result : results) {
            Objects.requireNonNull(result, "Individual DeltaDiffResult must not be null");
            totalOriginalSize += result.getOriginalSize();
            totalPatchedSize += result.getPatchedSize();
            totalDeltaSize += result.getDeltaSize();
            if (!result.isSuccess()) {
                anyFailed = true;
            }
            if (result.getWarnings() != null) {
                mergedWarnings.addAll(result.getWarnings());
            }
        }

        DeltaDiffResult merged = new DeltaDiffResult();
        merged.setOriginalSize(totalOriginalSize);
        merged.setPatchedSize(totalPatchedSize);
        merged.setDeltaSize(totalDeltaSize);
        merged.setSuccess(!anyFailed);
        merged.setWarnings(mergedWarnings);

        logger.fine("Merge complete: deltaSize=" + totalDeltaSize + ", success=" + !anyFailed);
        return merged;
    }

    /**
     * Returns true if all provided results represent successful diffs.
     */
    public boolean allSuccessful(List<DeltaDiffResult> results) {
        if (results == null || results.isEmpty()) return false;
        return results.stream().allMatch(DeltaDiffResult::isSuccess);
    }
}
