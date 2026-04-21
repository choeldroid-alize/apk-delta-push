package com.apkdeltapush.ratelimit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimitConfigTest {

    @Test
    void defaultConfig_hasExpectedValues() {
        RateLimitConfig config = RateLimitConfig.DEFAULT;
        assertEquals(10, config.getMaxPushesPerWindow());
        assertEquals(1_000L, config.getWindowMillis());
        assertTrue(config.isEnforced());
    }

    @Test
    void unlimitedConfig_isNotEnforced() {
        assertFalse(RateLimitConfig.UNLIMITED.isEnforced());
    }

    @Test
    void buildLimiter_returnsConfiguredLimiter() {
        RateLimitConfig config = new RateLimitConfig(5, 500L, true);
        RateLimiter limiter = config.buildLimiter();
        assertNotNull(limiter);
        assertEquals(5, limiter.getMaxTokens());
        assertEquals(500L, limiter.getWindowMillis());
    }

    @Test
    void constructor_invalidMaxPushes_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new RateLimitConfig(0, 1_000L, true));
    }

    @Test
    void constructor_invalidWindowMillis_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new RateLimitConfig(5, -1L, true));
    }

    @Test
    void toString_containsRelevantInfo() {
        RateLimitConfig config = new RateLimitConfig(3, 750L, false);
        String str = config.toString();
        assertTrue(str.contains("3"));
        assertTrue(str.contains("750"));
        assertTrue(str.contains("false"));
    }
}
