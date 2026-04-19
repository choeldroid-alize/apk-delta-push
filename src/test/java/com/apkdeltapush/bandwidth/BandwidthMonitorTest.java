package com.apkdeltapush.bandwidth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BandwidthMonitorTest {

    private BandwidthMonitor monitor;

    @BeforeEach
    void setUp() {
        monitor = new BandwidthMonitor();
    }

    @Test
    void testInitialState() {
        assertFalse(monitor.isRunning());
        assertEquals(0, monitor.getTotalBytesTransferred());
    }

    @Test
    void testStartSetsRunning() {
        monitor.start();
        assertTrue(monitor.isRunning());
    }

    @Test
    void testRecordTransferAccumulatesBytes() {
        monitor.start();
        monitor.recordTransfer(1024);
        monitor.recordTransfer(2048);
        assertEquals(3072, monitor.getTotalBytesTransferred());
    }

    @Test
    void testRecordNegativeBytesThrows() {
        monitor.start();
        assertThrows(IllegalArgumentException.class, () -> monitor.recordTransfer(-1));
    }

    @Test
    void testStopEndsRunning() {
        monitor.start();
        monitor.stop();
        assertFalse(monitor.isRunning());
    }

    @Test
    void testStopWithoutStartThrows() {
        assertThrows(IllegalStateException.class, () -> monitor.stop());
    }

    @Test
    void testGetSummaryContainsCorrectData() throws InterruptedException {
        monitor.start();
        monitor.recordTransfer(4096);
        Thread.sleep(50);
        monitor.stop();

        BandwidthSummary summary = monitor.getSummary();
        assertEquals(4096, summary.getTotalBytesTransferred());
        assertTrue(summary.getElapsedMs() >= 50);
        assertTrue(summary.getAverageRateBytesPerSecond() > 0);
        assertNotNull(summary.getFormattedRate());
    }

    @Test
    void testStartResetsBytes() {
        monitor.start();
        monitor.recordTransfer(500);
        monitor.stop();
        monitor.start();
        assertEquals(0, monitor.getTotalBytesTransferred());
    }

    @Test
    void testFormattedRateMegabytes() {
        BandwidthSummary summary = new BandwidthSummary(10 * 1024 * 1024, 2.0 * 1024 * 1024, 0, 5000);
        assertTrue(summary.getFormattedRate().contains("MB/s"));
    }
}
