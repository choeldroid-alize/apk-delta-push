package com.apkdeltapush.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PushLockManagerTest {

    private PushLockManager lockManager;

    @BeforeEach
    void setUp() {
        lockManager = new PushLockManager();
    }

    @Test
    void acquireLock_succeeds_whenDeviceNotLocked() {
        assertTrue(lockManager.acquireLock("device-1", "session-A"));
        assertTrue(lockManager.isLocked("device-1"));
    }

    @Test
    void acquireLock_fails_whenDeviceAlreadyLocked() {
        lockManager.acquireLock("device-1", "session-A");
        assertFalse(lockManager.acquireLock("device-1", "session-B"));
    }

    @Test
    void releaseLock_succeeds_forOwner() {
        lockManager.acquireLock("device-1", "session-A");
        lockManager.releaseLock("device-1", "session-A");
        assertFalse(lockManager.isLocked("device-1"));
    }

    @Test
    void releaseLock_throws_forNonOwner() {
        lockManager.acquireLock("device-1", "session-A");
        assertThrows(PushLockException.class, () -> lockManager.releaseLock("device-1", "session-B"));
    }

    @Test
    void releaseLock_throws_whenNoLockExists() {
        assertThrows(PushLockException.class, () -> lockManager.releaseLock("device-999", "session-A"));
    }

    @Test
    void getLockOwner_returnsCorrectSession() {
        lockManager.acquireLock("device-1", "session-A");
        assertEquals("session-A", lockManager.getLockOwner("device-1"));
    }

    @Test
    void forceRelease_unlocksDevice() {
        lockManager.acquireLock("device-1", "session-A");
        lockManager.forceRelease("device-1");
        assertFalse(lockManager.isLocked("device-1"));
        assertNull(lockManager.getLockOwner("device-1"));
    }

    @Test
    void multipleDifferentDevices_canBeLocked_independently() {
        assertTrue(lockManager.acquireLock("device-1", "session-A"));
        assertTrue(lockManager.acquireLock("device-2", "session-B"));
        assertTrue(lockManager.isLocked("device-1"));
        assertTrue(lockManager.isLocked("device-2"));
    }
}
