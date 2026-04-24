package com.apkdeltapush.dedup;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Deduplicates delta patches by tracking content hashes of previously
 * generated deltas. Avoids redundant diff computation and transfer when
 * the same source→target APK pair has already been processed.
 */
public class DeltaDeduplicator {

    private static final Logger LOG = Logger.getLogger(DeltaDeduplicator.class.getName());

    // key: "<sourceHash>:<targetHash>", value: cached delta entry
    private final Map<String, DedupEntry> index = new HashMap<>();

    /**
     * Looks up a previously stored delta for the given source/target hash pair.
     *
     * @param sourceHash SHA-256 hex of the source APK
     * @param targetHash SHA-256 hex of the target APK
     * @return an Optional containing the DedupEntry if a match exists
     */
    public Optional<DedupEntry> lookup(String sourceHash, String targetHash) {
        String key = buildKey(sourceHash, targetHash);
        DedupEntry entry = index.get(key);
        if (entry != null) {
            LOG.fine("Dedup hit for key: " + key);
        }
        return Optional.ofNullable(entry);
    }

    /**
     * Registers a delta patch in the deduplication index.
     *
     * @param sourceHash SHA-256 hex of the source APK
     * @param targetHash SHA-256 hex of the target APK
     * @param deltaPath  local filesystem path to the generated delta file
     * @param deltaSize  size of the delta in bytes
     */
    public void register(String sourceHash, String targetHash, String deltaPath, long deltaSize) {
        String key = buildKey(sourceHash, targetHash);
        DedupEntry entry = new DedupEntry(sourceHash, targetHash, deltaPath, deltaSize);
        index.put(key, entry);
        LOG.info("Registered dedup entry for key: " + key + " (" + deltaSize + " bytes)");
    }

    /**
     * Removes a dedup entry, e.g. when the underlying delta file has been evicted.
     *
     * @param sourceHash SHA-256 hex of the source APK
     * @param targetHash SHA-256 hex of the target APK
     * @return true if an entry was removed
     */
    public boolean evict(String sourceHash, String targetHash) {
        String key = buildKey(sourceHash, targetHash);
        boolean removed = index.remove(key) != null;
        if (removed) {
            LOG.info("Evicted dedup entry for key: " + key);
        }
        return removed;
    }

    /** Returns the number of entries currently tracked. */
    public int size() {
        return index.size();
    }

    /** Clears all dedup entries. */
    public void clear() {
        index.clear();
        LOG.info("DeltaDeduplicator index cleared.");
    }

    private String buildKey(String sourceHash, String targetHash) {
        return sourceHash + ":" + targetHash;
    }
}
