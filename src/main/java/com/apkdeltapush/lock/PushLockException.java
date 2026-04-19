package com.apkdeltapush.lock;

/**
 * Exception thrown when a push lock operation fails.
 */
public class PushLockException extends RuntimeException {

    private final String deviceId;

    public PushLockException(String message) {
        super(message);
        this.deviceId = null;
    }

    public PushLockException(String deviceId, String message) {
        super(message);
        this.deviceId = deviceId;
    }

    public PushLockException(String deviceId, String message, Throwable cause) {
        super(message, cause);
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
