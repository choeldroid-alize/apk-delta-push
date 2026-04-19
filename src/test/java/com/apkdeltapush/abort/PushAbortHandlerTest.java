package com.apkdeltapush.abort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PushAbortHandlerTest {

    private PushAbortHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PushAbortHandler();
    }

    @Test
    void testInitialStateNotAborted() {
        assertFalse(handler.isAborted());
        assertNull(handler.getLastReason());
    }

    @Test
    void testAbortSetsState() {
        handler.abort(AbortReason.USER_REQUESTED);
        assertTrue(handler.isAborted());
        assertEquals(AbortReason.USER_REQUESTED, handler.getLastReason());
    }

    @Test
    void testAbortNotifiesListeners() {
        List<AbortReason> received = new ArrayList<>();
        handler.registerListener(received::add);
        handler.abort(AbortReason.DEVICE_DISCONNECTED);
        assertEquals(1, received.size());
        assertEquals(AbortReason.DEVICE_DISCONNECTED, received.get(0));
    }

    @Test
    void testDuplicateAbortIgnored() {
        List<AbortReason> received = new ArrayList<>();
        handler.registerListener(received::add);
        handler.abort(AbortReason.TIMEOUT);
        handler.abort(AbortReason.INTERNAL_ERROR);
        assertEquals(1, received.size());
        assertEquals(AbortReason.TIMEOUT, handler.getLastReason());
    }

    @Test
    void testResetClearsState() {
        handler.abort(AbortReason.QUOTA_EXCEEDED);
        handler.reset();
        assertFalse(handler.isAborted());
        assertNull(handler.getLastReason());
    }

    @Test
    void testListenerExceptionDoesNotPropagateToOtherListeners() {
        List<AbortReason> received = new ArrayList<>();
        handler.registerListener(r -> { throw new RuntimeException("boom"); });
        handler.registerListener(received::add);
        assertDoesNotThrow(() -> handler.abort(AbortReason.CHECKSUM_MISMATCH));
        assertEquals(1, received.size());
    }

    @Test
    void testRegisterNullListenerThrows() {
        assertThrows(IllegalArgumentException.class, () -> handler.registerListener(null));
    }
}
