package com.apkdeltapush.history;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Manages the in-memory push history log with querying and eviction support.
 */
public class PushHistoryManager {

    private static final int DEFAULT_MAX_RECORDS = 500;

    private final int maxRecords;
    private final List<PushHistoryRecord> records;

    public PushHistoryManager() {
        this(DEFAULT_MAX_RECORDS);
    }

    public PushHistoryManager(int maxRecords) {
        if (maxRecords <= 0) throw new IllegalArgumentException("maxRecords must be positive");
        this.maxRecords = maxRecords;
        this.records = new CopyOnWriteArrayList<>();
    }

    /**
     * Adds a record to history. Evicts the oldest entry if capacity is exceeded.
     */
    public synchronized void addRecord(PushHistoryRecord record) {
        Objects.requireNonNull(record, "record must not be null");
        if (records.size() >= maxRecords) {
            records.remove(0);
        }
        records.add(record);
    }

    /**
     * Returns all history records in insertion order (oldest first).
     */
    public List<PushHistoryRecord> getAllRecords() {
        return Collections.unmodifiableList(new ArrayList<>(records));
    }

    /**
     * Returns history records filtered by device serial.
     */
    public List<PushHistoryRecord> getRecordsByDevice(String deviceSerial) {
        Objects.requireNonNull(deviceSerial, "deviceSerial must not be null");
        return records.stream()
                .filter(r -> deviceSerial.equals(r.getDeviceSerial()))
                .collect(Collectors.toList());
    }

    /**
     * Returns history records filtered by package name.
     */
    public List<PushHistoryRecord> getRecordsByPackage(String packageName) {
        Objects.requireNonNull(packageName, "packageName must not be null");
        return records.stream()
                .filter(r -> packageName.equals(r.getPackageName()))
                .collect(Collectors.toList());
    }

    /**
     * Returns only failed push records.
     */
    public List<PushHistoryRecord> getFailedRecords() {
        return records.stream()
                .filter(r -> !r.isSuccess())
                .collect(Collectors.toList());
    }

    /**
     * Finds a record by its unique ID.
     */
    public Optional<PushHistoryRecord> findById(String recordId) {
        Objects.requireNonNull(recordId, "recordId must not be null");
        return records.stream()
                .filter(r -> recordId.equals(r.getRecordId()))
                .findFirst();
    }

    /**
     * Clears all history records.
     */
    public synchronized void clear() {
        records.clear();
    }

    public int size() {
        return records.size();
    }

    public int getMaxRecords() {
        return maxRecords;
    }
}
