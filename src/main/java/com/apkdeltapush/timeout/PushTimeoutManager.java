package com.apkdeltapush.timeout;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages push operation timeouts per device session.
 */
public class PushTimeoutManager {

    private final Duration defaultTimeout;
    private final Map<String, Instant> deadlines = new ConcurrentHashMap<>();

    public PushTimeoutManager(Duration defaultTimeout) {
        if (defaultTimeout == null || defaultTimeout.isNegative() || defaultTimeout.isZero()) {
            throw new IllegalArgumentException("Timeout must be a positive duration");
        }
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * Registers a timeout for the given session using the default duration.
     */
    public void register(String sessionId) {
        register(sessionId, defaultTimeout);
    }

    /**
     * Registers a timeout for the given session using a custom duration.
     */
    public void register(String sessionId, Duration timeout) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID must not be null or blank");
        }
        deadlines.put(sessionId, Instant.now().plus(timeout));
    }

    /**
     * Returns true if the session has exceeded its deadline.
     */
    public boolean isTimedOut(String sessionId) {
        Instant deadline = deadlines.get(sessionId);
        if (deadline == null) {
            throw new IllegalStateException("No timeout registered for session: " + sessionId);
        }
        return Instant.now().isAfter(deadline);
    }

    /**
     * Returns remaining time for a session, or Duration.ZERO if already timed out.
     */
    public Duration remaining(String sessionId) {
        Instant deadline = deadlines.get(sessionId);
        if (deadline == null) {
            throw new IllegalStateException("No timeout registered for session: " + sessionId);
        }
        Duration left = Duration.between(Instant.now(), deadline);
        return left.isNegative() ? Duration.ZERO : left;
    }

    /**
     * Clears the timeout entry for a session.
     */
    public void clear(String sessionId) {
        deadlines.remove(sessionId);
    }

    public Duration getDefaultTimeout() {
        return defaultTimeout;
    }
}
