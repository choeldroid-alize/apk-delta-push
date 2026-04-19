package com.apkdeltapush.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages per-device push locks to prevent concurrent pushes to the same device.
 */
public class PushLockManager {

    private final Map<String, ReentrantLock> deviceLocks = new ConcurrentHashMap<>();
    private final Map<String, String> lockOwners = new ConcurrentHashMap<>();

    public boolean acquireLock(String deviceId, String sessionId) {
        deviceLocks.putIfAbsent(deviceId, new ReentrantLock());
        ReentrantLock lock = deviceLocks.get(deviceId);
        if (lock.tryLock()) {
            lockOwners.put(deviceId, sessionId);
            return true;
        }
        return false;
    }

    public void releaseLock(String deviceId, String sessionId) {
        ReentrantLock lock = deviceLocks.get(deviceId);
        if (lock == null) {
            throw new PushLockException("No lock found for device: " + deviceId);
        }
        String owner = lockOwners.get(deviceId);
        if (!sessionId.equals(owner)) {
            throw new PushLockException("Session " + sessionId + " does not own lock for device " + deviceId);
        }
        lockOwners.remove(deviceId);
        lock.unlock();
    }

    public boolean isLocked(String deviceId) {
        ReentrantLock lock = deviceLocks.get(deviceId);
        return lock != null && lock.isLocked();
    }

    public String getLockOwner(String deviceId) {
        return lockOwners.get(deviceId);
    }

    public void forceRelease(String deviceId) {
        ReentrantLock lock = deviceLocks.get(deviceId);
        if (lock != null && lock.isLocked()) {
            lockOwners.remove(deviceId);
            lock.unlock();
        }
    }
}
