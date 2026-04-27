package com.apkdeltapush.checkpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PushCheckpointManagerTest {

    private PushCheckpointManager manager;
    private AtomicInteger counter;

    @BeforeEach
    void setUp() {
        counter = new AtomicInteger(0);
        // Deterministic ID generator for predictable assertions
        CheckpointIdGenerator idGen = new CheckpointIdGenerator() {
            @Override
            public String generate() {
                return "ckpt-" + counter.incrementAndGet();
            }
        };
        manager = new PushCheckpointManager(idGen);
    }

    @Test
    void capture_storesCheckpointForSession() {
        PushCheckpoint cp = manager.capture("session-1", "emulator-5554",
                "com.example.app", 1024L, 3, CheckpointPhase.TRANSFER_IN_PROGRESS);

        assertNotNull(cp);
        assertEquals("ckpt-1", cp.getCheckpointId());
        assertEquals("session-1", cp.getSessionId());
        assertEquals("emulator-5554", cp.getDeviceSerial());
        assertEquals("com.example.app", cp.getPackageName());
        assertEquals(1024L, cp.getBytesTransferred());
        assertEquals(3, cp.getFragmentIndex());
        assertEquals(CheckpointPhase.TRANSFER_IN_PROGRESS, cp.getPhase());
        assertNotNull(cp.getCapturedAt());
    }

    @Test
    void getLatest_returnsEmptyWhenNoCheckpoint() {
        Optional<PushCheckpoint> result = manager.getLatest("unknown-session");
        assertTrue(result.isEmpty());
    }

    @Test
    void getLatest_returnsLatestCheckpointAfterCapture() {
        manager.capture("session-2", "device-A", "com.foo", 0L, 0, CheckpointPhase.PRE_TRANSFER);
        PushCheckpoint second = manager.capture("session-2", "device-A", "com.foo", 512L, 2, CheckpointPhase.TRANSFER_IN_PROGRESS);

        Optional<PushCheckpoint> result = manager.getLatest("session-2");
        assertTrue(result.isPresent());
        assertEquals(second.getCheckpointId(), result.get().getCheckpointId());
        assertEquals(2, result.get().getFragmentIndex());
    }

    @Test
    void hasCheckpoint_returnsTrueAfterCapture() {
        assertFalse(manager.hasCheckpoint("session-3"));
        manager.capture("session-3", "device-B", "com.bar", 0L, 0, CheckpointPhase.PRE_APPLY);
        assertTrue(manager.hasCheckpoint("session-3"));
    }

    @Test
    void invalidate_removesCheckpointAndReturnsTrueWhenPresent() {
        manager.capture("session-4", "device-C", "com.baz", 0L, 0, CheckpointPhase.PRE_VERIFY);
        assertTrue(manager.invalidate("session-4"));
        assertFalse(manager.hasCheckpoint("session-4"));
    }

    @Test
    void invalidate_returnsFalseWhenNoCheckpointExists() {
        assertFalse(manager.invalidate("nonexistent-session"));
    }

    @Test
    void getAllCheckpoints_returnsSnapshotOfAllSessions() {
        manager.capture("s1", "d1", "pkg1", 0L, 0, CheckpointPhase.PRE_TRANSFER);
        manager.capture("s2", "d2", "pkg2", 0L, 0, CheckpointPhase.PRE_TRANSFER);

        Map<String, PushCheckpoint> all = manager.getAllCheckpoints();
        assertEquals(2, all.size());
        assertTrue(all.containsKey("s1"));
        assertTrue(all.containsKey("s2"));
    }

    @Test
    void clearAll_removesAllCheckpoints() {
        manager.capture("s1", "d1", "pkg1", 0L, 0, CheckpointPhase.PRE_TRANSFER);
        manager.capture("s2", "d2", "pkg2", 0L, 0, CheckpointPhase.PRE_TRANSFER);
        manager.clearAll();

        assertTrue(manager.getAllCheckpoints().isEmpty());
    }
}
