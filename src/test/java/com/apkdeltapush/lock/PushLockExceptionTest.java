package com.apkdeltapush.lock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PushLockExceptionTest {

    @Test
    void constructor_withMessageOnly_setsMessage() {
        PushLockException ex = new PushLockException("lock error");
        assertEquals("lock error", ex.getMessage());
        assertNull(ex.getDeviceId());
    }

    @Test
    void constructor_withDeviceIdAndMessage_setsFields() {
        PushLockException ex = new PushLockException("device-1", "device is locked");
        assertEquals("device-1", ex.getDeviceId());
        assertEquals("device is locked", ex.getMessage());
    }

    @Test
    void constructor_withCause_setsCause() {
        Throwable cause = new RuntimeException("root cause");
        PushLockException ex = new PushLockException("device-2", "lock failed", cause);
        assertEquals(cause, ex.getCause());
        assertEquals("device-2", ex.getDeviceId());
    }
}
