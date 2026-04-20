package com.apkdeltapush.audit;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable record capturing a single auditable push event.
 */
public final class PushAuditRecord {

    private final String sessionId;
    private final String deviceSerial;
    private final String packageName;
    private final AuditEventType eventType;
    private final String detail;
    private final Instant timestamp;

    public PushAuditRecord(String sessionId,
                           String deviceSerial,
                           String packageName,
                           AuditEventType eventType,
                           String detail) {
        this.sessionId    = Objects.requireNonNull(sessionId,    "sessionId");
        this.deviceSerial = Objects.requireNonNull(deviceSerial, "deviceSerial");
        this.packageName  = Objects.requireNonNull(packageName,  "packageName");
        this.eventType    = Objects.requireNonNull(eventType,    "eventType");
        this.detail       = detail != null ? detail : "";
        this.timestamp    = Instant.now();
    }

    public String getSessionId()    { return sessionId; }
    public String getDeviceSerial() { return deviceSerial; }
    public String getPackageName()  { return packageName; }
    public AuditEventType getEventType() { return eventType; }
    public String getDetail()       { return detail; }
    public Instant getTimestamp()   { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s | device=%s pkg=%s detail=%s",
                timestamp, eventType, deviceSerial, packageName, detail);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PushAuditRecord)) return false;
        PushAuditRecord that = (PushAuditRecord) o;
        return sessionId.equals(that.sessionId)
                && deviceSerial.equals(that.deviceSerial)
                && packageName.equals(that.packageName)
                && eventType == that.eventType
                && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, deviceSerial, packageName, eventType, timestamp);
    }
}
