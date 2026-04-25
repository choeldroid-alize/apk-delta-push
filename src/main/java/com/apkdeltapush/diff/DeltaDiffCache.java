package com.apkdeltapush.diff;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LRU cache for DeltaDiffResult objects, keyed by a composite of source and target APK checksums.
 * Avoids redundant diff computations for identical APK pairs within a session.
 */
public class DeltaDiffCache {

    private static final int DEFAULT_MAX_ENTRIES = 32;

    private final int maxEntries;
    private final Map<String, DeltaDiffResult> cache;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public DeltaDiffCache() {
        this(DEFAULT_MAX_ENTRIES);
    }

    public DeltaDiffCache(int maxEntries) {
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("maxEntries must be positive");
        }
        this.maxEntries = maxEntries;
        this.cache = new LinkedHashMap<>(maxEntries, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, DeltaDiffResult> eldest) {
                return size() > maxEntries;
            }
        };
    }

    public String buildKey(String sourceChecksum, String targetChecksum) {
        if (sourceChecksum == null || targetChecksum == null) {
            throw new IllegalArgumentException("Checksums must not be null");
        }
        return sourceChecksum + "::" + targetChecksum;
    }

    public void put(String sourceChecksum, String targetChecksum, DeltaDiffResult result) {
        String key = buildKey(sourceChecksum, targetChecksum);
        lock.writeLock().lock();
        try {
            cache.put(key, result);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<DeltaDiffResult> get(String sourceChecksum, String targetChecksum) {
        String key = buildKey(sourceChecksum, targetChecksum);
        lock.readLock().lock();
        try {
            return Optional.ofNullable(cache.get(key));
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean contains(String sourceChecksum, String targetChecksum) {
        String key = buildKey(sourceChecksum, targetChecksum);
        lock.readLock().lock();
        try {
            return cache.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void invalidate(String sourceChecksum, String targetChecksum) {
        String key = buildKey(sourceChecksum, targetChecksum);
        lock.writeLock().lock();
        try {
            cache.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getMaxEntries() {
        return maxEntries;
    }
}
