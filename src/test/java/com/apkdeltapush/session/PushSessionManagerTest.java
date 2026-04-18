package com.apkdeltapush.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PushSessionManagerTest {

    private PushSessionManager manager;

    @BeforeEach
    void setUp() {
        manager = new PushSessionManager();
    }

    @Test
    void createSession_shouldReturnSessionWithPendingStatus() {
        PushSession session = manager.createSession("emulator-5554", "com.example.app");
        assertNotNull(session);
        assertEquals(PushSession.Status.PENDING, session.getStatus());
        assertEquals("emulator-5554", session.getDeviceSerial());
        assertEquals("com.example.app", session.getPackageName());
    }

    @Test
    void createSession_shouldBeRetrievableById() {
        PushSession session = manager.createSession("emulator-5554", "com.example.app");
        Optional<PushSession> retrieved = manager.getSession(session.getSessionId());
        assertTrue(retrieved.isPresent());
        assertEquals(session.getSessionId(), retrieved.get().getSessionId());
    }

    @Test
    void createSession_shouldThrowOnBlankDevice() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.createSession("", "com.example.app"));
    }

    @Test
    void createSession_shouldThrowOnNullPackage() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.createSession("emulator-5554", null));
    }

    @Test
    void clearFinishedSessions_shouldRemoveCompletedAndFailed() {
        PushSession s1 = manager.createSession("device-1", "com.a");
        PushSession s2 = manager.createSession("device-2", "com.b");
        PushSession s3 = manager.createSession("device-3", "com.c");

        s1.complete(1024);
        s2.fail("timeout");
        // s3 remains PENDING

        int removed = manager.clearFinishedSessions();
        assertEquals(2, removed);
        assertEquals(1, manager.sessionCount());
        assertTrue(manager.getSession(s3.getSessionId()).isPresent());
    }

    @Test
    void sessionLifecycle_completeSetsBytes() {
        PushSession session = manager.createSession("emulator-5554", "com.example");
        session.start();
        assertEquals(PushSession.Status.IN_PROGRESS, session.getStatus());
        session.complete(4096);
        assertEquals(PushSession.Status.COMPLETED, session.getStatus());
        assertEquals(4096, session.getBytesPushed());
        assertNotNull(session.getCompletedAt());
    }
}
