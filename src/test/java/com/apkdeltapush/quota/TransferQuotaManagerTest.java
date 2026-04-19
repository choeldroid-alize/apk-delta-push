package com.apkdeltapush.quota;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransferQuotaManagerTest {

    private TransferQuotaManager manager;

    @BeforeEach
    void setUp() {
        manager = new TransferQuotaManager(1000L);
    }

    @Test
    void constructor_throwsOnNonPositiveLimit() {
        assertThrows(IllegalArgumentException.class, () -> new TransferQuotaManager(0));
        assertThrows(IllegalArgumentException.class, () -> new TransferQuotaManager(-100));
    }

    @Test
    void recordTransfer_accumulates() throws QuotaExceededException {
        manager.recordTransfer(200);
        manager.recordTransfer(300);
        assertEquals(500L, manager.getBytesTransferred());
    }

    @Test
    void recordTransfer_throwsWhenQuotaExceeded() {
        assertThrows(QuotaExceededException.class, () -> {
            manager.recordTransfer(800);
            manager.recordTransfer(300);
        });
        assertTrue(manager.isQuotaExceeded());
    }

    @Test
    void getRemainingBytes_decreasesWithTransfers() throws QuotaExceededException {
        manager.recordTransfer(400);
        assertEquals(600L, manager.getRemainingBytes());
    }

    @Test
    void getRemainingBytes_neverNegative() throws QuotaExceededException {
        manager.recordTransfer(999);
        assertEquals(1L, manager.getRemainingBytes());
    }

    @Test
    void getUsagePercent_calculatesCorrectly() throws QuotaExceededException {
        manager.recordTransfer(500);
        assertEquals(50.0, manager.getUsagePercent(), 0.001);
    }

    @Test
    void reset_clearsState() throws QuotaExceededException {
        manager.recordTransfer(500);
        manager.reset();
        assertEquals(0L, manager.getBytesTransferred());
        assertFalse(manager.isQuotaExceeded());
    }

    @Test
    void getSummary_reflectsCurrentState() throws QuotaExceededException {
        manager.recordTransfer(250);
        QuotaSummary summary = manager.getSummary();
        assertEquals(250L, summary.getUsedBytes());
        assertEquals(1000L, summary.getMaxBytes());
        assertEquals(750L, summary.getRemainingBytes());
        assertFalse(summary.isExceeded());
        assertNotNull(summary.getSessionStart());
    }

    @Test
    void recordTransfer_throwsOnNegativeBytes() {
        assertThrows(IllegalArgumentException.class, () -> manager.recordTransfer(-1));
    }
}
