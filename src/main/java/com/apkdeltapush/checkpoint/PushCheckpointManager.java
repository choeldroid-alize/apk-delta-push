package com.apkdeltapush.checkpoint;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages creation, retrieval, and invalidation of push checkpoints.
 * Checkpoints allow interrupted push sessions to resume from the last
 * known-good state rather than restarting from scratch.
 */
public class PushCheckpointManager {

    private static final Logger logger = Logger.getLogger(PushCheckpointManager.class.getName());

    /** Key: sessionId -> latest checkpoint for that session. */
    private final Map<String, PushCheckpoint> checkpointStore = new ConcurrentHashMap<>();

    private final CheckpointIdGenerator idGenerator;

    public PushCheckpointManager(CheckpointIdGenerator idGenerator) {
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator must not be null");
    }

    /**
     * Captures a new checkpoint for the given session, overwriting any prior checkpoint.
     */
    public PushCheckpoint capture(String sessionId, String deviceSerial, String packageName,
                                  long bytesTransferred, int fragmentIndex, CheckpointPhase phase) {
        String id = idGenerator.generate();
        PushCheckpoint checkpoint = new PushCheckpoint(
                id, sessionId, deviceSerial, packageName,
                bytesTransferred, fragmentIndex, phase, Instant.now());
        checkpointStore.put(sessionId, checkpoint);
        logger.fine("Checkpoint captured: " + checkpoint);
        return checkpoint;
    }

    /**
     * Returns the latest checkpoint for the given session, if one exists.
     */
    public Optional<PushCheckpoint> getLatest(String sessionId) {
        return Optional.ofNullable(checkpointStore.get(sessionId));
    }

    /**
     * Removes the checkpoint for the given session (e.g., after successful completion).
     */
    public boolean invalidate(String sessionId) {
        boolean removed = checkpointStore.remove(sessionId) != null;
        if (removed) {
            logger.fine("Checkpoint invalidated for session: " + sessionId);
        }
        return removed;
    }

    /**
     * Returns true if a resumable checkpoint exists for the given session.
     */
    public boolean hasCheckpoint(String sessionId) {
        return checkpointStore.containsKey(sessionId);
    }

    /**
     * Returns an unmodifiable snapshot of all active checkpoints.
     */
    public Map<String, PushCheckpoint> getAllCheckpoints() {
        return Collections.unmodifiableMap(new HashMap<>(checkpointStore));
    }

    /**
     * Clears all checkpoints (e.g., on tool shutdown or full reset).
     */
    public void clearAll() {
        checkpointStore.clear();
        logger.info("All push checkpoints cleared.");
    }
}
