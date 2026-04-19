package com.apkdeltapush.conflict;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConflictResolverTest {

    private ConflictResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ConflictResolver();
    }

    @Test
    void testFirstPushProceedsWithoutConflict() {
        ConflictResolution result = resolver.registerPush("emulator-5554", "com.example.app", "session-1");
        assertTrue(result.canProceed());
        assertEquals(ConflictStrategy.PROCEED, result.getStrategy());
        assertEquals("session-1", result.getSessionId());
    }

    @Test
    void testSecondPushIsDeferred() {
        resolver.registerPush("emulator-5554", "com.example.app", "session-1");
        ConflictResolution result = resolver.registerPush("emulator-5554", "com.example.app", "session-2");
        assertFalse(result.canProceed());
        assertEquals(ConflictStrategy.DEFER, result.getStrategy());
        assertEquals("session-1", result.getSessionId());
    }

    @Test
    void testDifferentPackagesDoNotConflict() {
        resolver.registerPush("emulator-5554", "com.example.app", "session-1");
        ConflictResolution result = resolver.registerPush("emulator-5554", "com.example.other", "session-2");
        assertTrue(result.canProceed());
    }

    @Test
    void testDifferentDevicesDoNotConflict() {
        resolver.registerPush("emulator-5554", "com.example.app", "session-1");
        ConflictResolution result = resolver.registerPush("emulator-5556", "com.example.app", "session-2");
        assertTrue(result.canProceed());
    }

    @Test
    void testReleaseAllowsNextPush() {
        resolver.registerPush("emulator-5554", "com.example.app", "session-1");
        boolean released = resolver.releasePush("emulator-5554", "com.example.app", "session-1");
        assertTrue(released);
        ConflictResolution result = resolver.registerPush("emulator-5554", "com.example.app", "session-2");
        assertTrue(result.canProceed());
    }

    @Test
    void testReleaseWithWrongSessionReturnsFalse() {
        resolver.registerPush("emulator-5554", "com.example.app", "session-1");
        boolean released = resolver.releasePush("emulator-5554", "com.example.app", "session-99");
        assertFalse(released);
        assertTrue(resolver.hasConflict("emulator-5554", "com.example.app"));
    }

    @Test
    void testActiveConflictCount() {
        resolver.registerPush("emulator-5554", "com.example.app", "session-1");
        resolver.registerPush("emulator-5556", "com.example.app", "session-2");
        assertEquals(2, resolver.activeConflictCount());
    }
}
