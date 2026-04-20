package com.apkdeltapush.timeout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class PushTimeoutManagerTest {

    private PushTimeoutManager manager;

    @BeforeEach
    void setUp() {
        manager = new PushTimeoutManager(Duration.ofSeconds(30));
    }

    @Test
    void constructorRejectsNullTimeout() {
        assertThrows(IllegalArgumentException.class, () -> new PushTimeoutManager(null));
    }

    @Test
    void constructorRejectsZeroDuration() {
        assertThrows(IllegalArgumentException.class, () -> new PushTimeoutManager(Duration.ZERO));
    }

    @Test
    void constructorRejectsNegativeDuration() {
        assertThrows(IllegalArgumentException.class, () -> new PushTimeoutManager(Duration.ofSeconds(-1)));
    }

    @Test
    void registerAndIsNotTimedOut() {
        manager.register("session-1");
        assertFalse(manager.isTimedOut("session-1"));
    }

    @Test
    void registerWithShortTimeoutIsTimedOut() throws InterruptedException {
        manager.register("session-2", Duration.ofMillis(50));
        Thread.sleep(100);
        assertTrue(manager.isTimedOut("session-2"));
    }

    @Test
    void remainingIsPositiveForFreshSession() {
        manager.register("session-3");
        Duration remaining = manager.remaining("session-3");
        assertTrue(remaining.toMillis() > 0);
    }

    @Test
    void remainingIsZeroAfterExpiry() throws InterruptedException {
        manager.register("session-4", Duration.ofMillis(50));
        Thread.sleep(100);
        assertEquals(Duration.ZERO, manager.remaining("session-4"));
    }

    @Test
    void isTimedOutThrowsForUnknownSession() {
        assertThrows(IllegalStateException.class, () -> manager.isTimedOut("unknown"));
    }

    @Test
    void clearRemovesSession() {
        manager.register("session-5");
        manager.clear("session-5");
        assertThrows(IllegalStateException.class, () -> manager.isTimedOut("session-5"));
    }

    @Test
    void registerRejectsBlankSessionId() {
        assertThrows(IllegalArgumentException.class, () -> manager.register("  "));
    }

    @Test
    void getDefaultTimeoutReturnsConfiguredValue() {
        assertEquals(Duration.ofSeconds(30), manager.getDefaultTimeout());
    }
}
