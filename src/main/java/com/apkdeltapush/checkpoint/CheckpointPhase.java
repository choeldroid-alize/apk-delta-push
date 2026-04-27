package com.apkdeltapush.checkpoint;

/**
 * Represents the phase of a push operation at which a checkpoint was captured.
 */
public enum CheckpointPhase {
    /** Delta generation has completed; transfer not yet started. */
    PRE_TRANSFER,
    /** Transfer is in progress; some fragments have been sent. */
    TRANSFER_IN_PROGRESS,
    /** All fragments transferred; patch application pending. */
    PRE_APPLY,
    /** Patch application completed; verification pending. */
    PRE_VERIFY
}
