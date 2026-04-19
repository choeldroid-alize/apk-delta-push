package com.apkdeltapush.transfer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransferThrottlerTest {

    @Test
    void constructor_rejectsNonPositiveRate() {
        assertThrows(IllegalArgumentException.class, () -> new TransferThrottler(0));
        assertThrows(IllegalArgumentException.class, () -> new TransferThrottler(-1));
    }

    @Test
    void throttle_tracksTransferredBytes() throws InterruptedException {
        TransferThrottler throttler = new TransferThrottler(1_000_000);
        throttler.throttle(512);
        throttler.throttle(256);
        assertEquals(768, throttler.getBytesTransferredInWindow());
    }

    @Test
    void reset_clearsBytesInWindow() throws InterruptedException {
        TransferThrottler throttler = new TransferThrottler(1_000_000);
        throttler.throttle(1024);
        throttler.reset();
        assertEquals(0, throttler.getBytesTransferredInWindow());
    }

    @Test
    void throttle_doesNotBlockWhenUnderLimit() throws InterruptedException {
        TransferThrottler throttler = new TransferThrottler(10_000_000);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            throttler.throttle(100);
        }
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 500, "Should not block significantly when under limit");
    }

    @Test
    void getMaxBytesPerSecond_returnsConfiguredValue() {
        TransferThrottler throttler = new TransferThrottler(2048);
        assertEquals(2048, throttler.getMaxBytesPerSecond());
    }

    @Test
    void throttleConfig_unlimitedIsNotEnabled() {
        assertFalse(ThrottleConfig.UNLIMITED.isEnabled());
    }

    @Test
    void throttleConfig_of_createsEnabledConfig() {
        ThrottleConfig config = ThrottleConfig.of(512_000);
        assertTrue(config.isEnabled());
        assertEquals(512_000, config.getMaxBytesPerSecond());
    }

    @Test
    void throttleConfig_createThrottler_returnsThrottlerWithCorrectRate() {
        ThrottleConfig config = ThrottleConfig.of(1_024);
        TransferThrottler throttler = config.createThrottler();
        assertEquals(1_024, throttler.getMaxBytesPerSecond());
    }
}
