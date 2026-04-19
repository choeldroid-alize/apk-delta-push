package com.apkdeltapush.conflict;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Resolves conflicts when multiple push operations target the same device/package.
 */
public class ConflictResolver {

    private static final Logger logger = Logger.getLogger(ConflictResolver.class.getName());

    private final Map<String, PushConflict> activeConflicts = new HashMap<>();

    /**
     * Registers a push attempt for a device+package key.
     * Returns a ConflictResolution indicating whether to proceed or defer.
     */
    public ConflictResolution registerPush(String deviceSerial, String packageName, String sessionId) {
        String key = buildKey(deviceSerial, packageName);
        synchronized (activeConflicts) {
            if (activeConflicts.containsKey(key)) {
                PushConflict existing = activeConflicts.get(key);
                logger.warning("Conflict detected for " + key + ": session " + existing.getSessionId() + " already active");
                return new ConflictResolution(false, existing.getSessionId(), ConflictStrategy.DEFER);
            }
            activeConflicts.put(key, new PushConflict(sessionId, deviceSerial, packageName));
            return new ConflictResolution(true, sessionId, ConflictStrategy.PROCEED);
        }
    }

    /**
     * Releases the lock held by the given session for a device+package pair.
     */
    public boolean releasePush(String deviceSerial, String packageName, String sessionId) {
        String key = buildKey(deviceSerial, packageName);
        synchronized (activeConflicts) {
            PushConflict conflict = activeConflicts.get(key);
            if (conflict != null && conflict.getSessionId().equals(sessionId)) {
                activeConflicts.remove(key);
                logger.info("Released conflict lock for " + key + " by session " + sessionId);
                return true;
            }
            return false;
        }
    }

    public boolean hasConflict(String deviceSerial, String packageName) {
        return activeConflicts.containsKey(buildKey(deviceSerial, packageName));
    }

    public int activeConflictCount() {
        return activeConflicts.size();
    }

    private String buildKey(String deviceSerial, String packageName) {
        return deviceSerial + ":" + packageName;
    }
}
