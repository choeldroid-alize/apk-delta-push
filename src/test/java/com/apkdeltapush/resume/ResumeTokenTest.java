package com.apkdeltapush.resume;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResumeTokenTest {

    @Test
    void testProgressPercent() {
        ResumeToken token = new ResumeToken("device1", "com.example", 500L, 1000L, "abc123");
        assertEquals(50.0, token.getProgressPercent(), 0.01);
    }

    @Test
    void testProgressPercentZeroTotal() {
        ResumeToken token = new ResumeToken("device1", "com.example", 0L, 0L, "abc123");
        assertEquals(0.0, token.getProgressPercent(), 0.01);
    }

    @Test
    void testIsCompleteWhenFull() {
        ResumeToken token = new ResumeToken("device1", "com.example", 1000L, 1000L, "abc123");
        assertTrue(token.isComplete());
    }

    @Test
    void testIsNotCompleteWhenPartial() {
        ResumeToken token = new ResumeToken("device1", "com.example", 400L, 1000L, "abc123");
        assertFalse(token.isComplete());
    }

    @Test
    void testToString() {
        ResumeToken token = new ResumeToken("device1", "com.example", 250L, 1000L, "abc123");
        String str = token.toString();
        assertTrue(str.contains("device1"));
        assertTrue(str.contains("com.example"));
        assertTrue(str.contains("25.0%"));
    }

    @Test
    void testCreatedAtIsSet() {
        ResumeToken token = new ResumeToken("d", "p", 0L, 100L, "cs");
        assertNotNull(token.getCreatedAt());
    }
}
