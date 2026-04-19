package com.apkdeltapush.notify;

import java.time.Instant;

/**
 * Represents a notification event emitted during an APK push lifecycle.
 */
public class PushNotification {

    private final String deviceSerial;
    private final String packageName;
    private final PushNotificationType type;
    private final String message;
    private final Instant timestamp;

    public PushNotification(String deviceSerial, String packageName,
                            PushNotificationType type, String message) {
        this.deviceSerial = deviceSerial;
        this.packageName = packageName;
        this.type = type;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public String getDeviceSerial() { return deviceSerial; }
    public String getPackageName() { return packageName; }
    public PushNotificationType getType() { return type; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s: %s",
                timestamp, deviceSerial, packageName, type, message);
    }
}
