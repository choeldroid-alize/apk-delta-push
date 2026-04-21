package com.apkdeltapush.ratelimit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    @Test
    void tryAcquire_withinLimit_returnsTrue() {
        RateLimiter limiter = new RateLimiter(5, 1_000L);
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.tryAcquire(), "Expected acquire to succeed for token " + (i + 1));
        }
    }

    @Test
    void tryAcquire_exceedsLimit_returnsFalse() {
        RateLimiter limiter = new RateLimiter(3, 1_000L);
        limiter.tryAcquire();
        limiter.tryAcquire();
        limiter.tryAcquire();
        assertFalse(limiter.tryAcquire(), "Expected acquire to fail when limit exceeded");
    }

    @Test
    void availableTokens_initiallyEqualsMax() {
        RateLimiter limiter = new RateLimiter(4, 1_000L);
        assertEquals(4, limiter.availableTokens());
    }

    @Test
    void availableTokens_decreasesAfterAcquire() {
        RateLimiter limiter = new RateLimiter(4, 1_000L);
        limiter.tryAcquire();
        limiter.tryAcquire();
        assertEquals(2, limiter.availableTokens());
    }

    @Test
    void millisUntilNextToken_isZeroWhenTokensAvailable() {
        RateLimiter limiter = new RateLimiter(3, 1_000L);
        assertEquals(0L, limiter.millisUntilNextToken());
    }

    @Test
    void millisUntilNextToken_isPositiveWhenExhausted() {
        RateLimiter limiter = new RateLimiter(1, 2_000L);
        limiter.tryAcquire();
        long wait = limiter.millisUntilNextToken();
        assertTrue(wait > 0 && wait <= 2_000L,
                "Expected wait between 1 and 2000ms, got " + wait);
    }

    @Test
    void constructor_invalidMaxTokens_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new RateLimiter(0, 1_000L));
    }

    @Test
    void constructor_invalidWindowMillis_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new RateLimiter(5, 0L));
    }

    @Test
    void getters_returnConfiguredValues() {
        RateLimiter limiter = new RateLimiter(7, 500L);
        assertEquals(7, limiter.getMaxTokens());
        assertEquals(500L, limiter.getWindowMillis());
    }
}
