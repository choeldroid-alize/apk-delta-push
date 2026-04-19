package com.apkdeltapush.log;

import java.time.Instant;

public class PushEvent {

    private final PushEventType type;
    private final String deviceSerial;
    private final String message;
    private final Instant timestamp;

    public PushEvent(PushEventType type, String deviceSerial, String message, Instant timestamp) {
        this.type = type;
        this.deviceSerial = deviceSerial;
        this.message = message;
        this.timestamp = timestamp;
    }

    public PushEventType getType() { return type; }
    public String getDeviceSerial() { return deviceSerial; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return timestamp + " [" + type + "] " + deviceSerial + " - " + message;
    }
}
