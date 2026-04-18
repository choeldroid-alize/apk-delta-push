package com.apkdeltapush.session;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages lifecycle of all active and historical PushSession instances.
 */
public class PushSessionManager {

    private final Map<String, PushSession> sessions = new ConcurrentHashMap<>();

    /**
     * Creates and registers a new session for the given device and package.
     */
    public PushSession createSession(String deviceSerial, String packageName) {
        if (deviceSerial == null || deviceSerial.isBlank()) {
            throw new IllegalArgumentException("deviceSerial must not be blank");
        }
        if (packageName == null || packageName.isBlank()) {
            throw new IllegalArgumentException("packageName must not be blank");
        }
        PushSession session = new PushSession(deviceSerial, packageName);
        sessions.put(session.getSessionId(), session);
        return session;
    }

    /**
     * Retrieves a session by its ID.
     */
    public Optional<PushSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * Returns all tracked sessions (unmodifiable view).
     */
    public Collection<PushSession> getAllSessions() {
        return Collections.unmodifiableCollection(sessions.values());
    }

    /**
     * Removes completed or failed sessions from the registry.
     */
    public int clearFinishedSessions() {
        int[] count = {0};
        sessions.entrySet().removeIf(entry -> {
            PushSession.Status s = entry.getValue().getStatus();
            if (s == PushSession.Status.COMPLETED || s == PushSession.Status.FAILED) {
                count[0]++;
                return true;
            }
            return false;
        });
        return count[0];
    }

    public int sessionCount() {
        return sessions.size();
    }
}
