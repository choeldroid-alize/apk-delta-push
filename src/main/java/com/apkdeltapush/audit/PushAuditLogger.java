package com.apkdeltapush.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Thread-safe logger that accumulates {@link PushAuditRecord}s for the
 * lifetime of the process and supports querying by session or device.
 */
public class PushAuditLogger {

    private final List<PushAuditRecord> records = new CopyOnWriteArrayList<>();

    /**
     * Append a new audit record.
     */
    public void log(String sessionId,
                    String deviceSerial,
                    String packageName,
                    AuditEventType eventType,
                    String detail) {
        PushAuditRecord record =
                new PushAuditRecord(sessionId, deviceSerial, packageName, eventType, detail);
        records.add(record);
    }

    /**
     * Convenience overload without detail message.
     */
    public void log(String sessionId,
                    String deviceSerial,
                    String packageName,
                    AuditEventType eventType) {
        log(sessionId, deviceSerial, packageName, eventType, null);
    }

    /**
     * Returns all records for a given session, in insertion order.
     */
    public List<PushAuditRecord> getRecordsForSession(String sessionId) {
        if (sessionId == null) return Collections.emptyList();
        return records.stream()
                .filter(r -> sessionId.equals(r.getSessionId()))
                .collect(Collectors.toList());
    }

    /**
     * Returns all records for a given device serial, in insertion order.
     */
    public List<PushAuditRecord> getRecordsForDevice(String deviceSerial) {
        if (deviceSerial == null) return Collections.emptyList();
        return records.stream()
                .filter(r -> deviceSerial.equals(r.getDeviceSerial()))
                .collect(Collectors.toList());
    }

    /**
     * Returns an unmodifiable snapshot of all records.
     */
    public List<PushAuditRecord> getAllRecords() {
        return Collections.unmodifiableList(new ArrayList<>(records));
    }

    /**
     * Clears all stored records (useful between test runs).
     */
    public void clear() {
        records.clear();
    }

    public int size() {
        return records.size();
    }
}
