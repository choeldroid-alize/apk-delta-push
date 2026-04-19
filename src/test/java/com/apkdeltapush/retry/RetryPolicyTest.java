package com.apkdeltapush.retry;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryPolicyTest {

    @Test
    void testSuccessOnFirstAttempt() throws Exception {
        RetryPolicy policy = RetryPolicy.noRetry();
        String result = policy.execute(() -> "ok");
        assertEquals("ok", result);
    }

    @Test
    void testRetriesOnFailure() throws Exception {
        RetryPolicy policy = new RetryPolicy(3, 0, 1.0);
        AtomicInteger calls = new AtomicInteger(0);
        String result = policy.execute(() -> {
            if (calls.incrementAndGet() < 3) throw new RuntimeException("fail");
            return "success";
        });
        assertEquals("success", result);
        assertEquals(3, calls.get());
    }

    @Test
    void testExhaustsRetries() {
        RetryPolicy policy = new RetryPolicy(2, 0, 1.0);
        AtomicInteger calls = new AtomicInteger(0);
        assertThrows(RetryExhaustedException.class, () ->
            policy.execute(() -> {
                calls.incrementAndGet();
                throw new RuntimeException("always fails");
            })
        );
        assertEquals(2, calls.get());
    }

    @Test
    void testDefaultPolicyParameters() {
        RetryPolicy policy = RetryPolicy.defaultPolicy();
        assertEquals(3, policy.getMaxAttempts());
        assertEquals(500, policy.getInitialDelayMs());
        assertEquals(2.0, policy.getBackoffMultiplier());
    }

    @Test
    void testInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(0, 100, 1.0));
    }

    @Test
    void testInvalidBackoffMultiplier() {
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(3, 100, 0.5));
    }

    @Test
    void testRetryExhaustedCause() {
        RetryPolicy policy = new RetryPolicy(1, 0, 1.0);
        RuntimeException cause = new RuntimeException("root cause");
        RetryExhaustedException ex = assertThrows(RetryExhaustedException.class, () ->
            policy.execute(() -> { throw cause; })
        );
        assertEquals(cause, ex.getCause());
    }
}
