package com.apkdeltapush.abort;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbortReasonTest {

    @Test
    void testAllReasonsHaveDescriptions() {
        for (AbortReason reason : AbortReason.values()) {
            assertNotNull(reason.getDescription());
            assertFalse(reason.getDescription().isBlank());
        }
    }

    @Test
    void testToStringContainsNameAndDescription() {
        AbortReason reason = AbortReason.NETWORK_FAILURE;
        String str = reason.toString();
        assertTrue(str.contains("NETWORK_FAILURE"));
        assertTrue(str.contains(reason.getDescription()));
    }

    @Test
    void testEnumValues() {
        assertEquals(7, AbortReason.values().length);
    }
}
