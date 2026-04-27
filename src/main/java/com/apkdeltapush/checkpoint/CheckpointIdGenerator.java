package com.apkdeltapush.checkpoint;

import java.util.UUID;

/**
 * Generates unique identifiers for push checkpoints.
 * Extracted as a separate class to allow deterministic IDs in tests.
 */
public class CheckpointIdGenerator {

    /**
     * Generates a new unique checkpoint ID.
     *
     * @return a non-null, non-empty string ID
     */
    public String generate() {
        return "ckpt-" + UUID.randomUUID().toString().replace("-", "");
    }
}
