package com.apkdeltapush.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NetworkConditionMonitorTest {

    private NetworkProbe probe;
    private NetworkConditionMonitor monitor;

    @BeforeEach
    void setUp() {
        probe = mock(NetworkProbe.class);
        monitor = new NetworkConditionMonitor(probe);
    }

    @Test
    void getCurrentCondition_callsProbeOnFirstAccess() {
        NetworkCondition condition = new NetworkCondition(20, 1_000_000, Instant.now());
        when(probe.measure()).thenReturn(condition);

        NetworkCondition result = monitor.getCurrentCondition();

        assertSame(condition, result);
        verify(probe, times(1)).measure();
    }

    @Test
    void getCurrentCondition_returnsCachedValueWhenFresh() {
        NetworkCondition condition = new NetworkCondition(20, 1_000_000, Instant.now());
        when(probe.measure()).thenReturn(condition);

        monitor.getCurrentCondition();
        monitor.getCurrentCondition();

        verify(probe, times(1)).measure();
    }

    @Test
    void refresh_alwaysCallsProbe() {
        NetworkCondition c1 = new NetworkCondition(10, 500_000, Instant.now());
        NetworkCondition c2 = new NetworkCondition(15, 600_000, Instant.now());
        when(probe.measure()).thenReturn(c1, c2);

        monitor.refresh();
        NetworkCondition result = monitor.refresh();

        assertSame(c2, result);
        verify(probe, times(2)).measure();
    }

    @Test
    void isConnectionHealthy_trueForGoodConditions() {
        when(probe.measure()).thenReturn(new NetworkCondition(100, 2_000_000, Instant.now()));
        assertTrue(monitor.isConnectionHealthy());
    }

    @Test
    void isConnectionHealthy_falseForHighLatency() {
        when(probe.measure()).thenReturn(new NetworkCondition(600, 2_000_000, Instant.now()));
        assertFalse(monitor.isConnectionHealthy());
    }

    @Test
    void isConnectionHealthy_falseForZeroThroughput() {
        when(probe.measure()).thenReturn(new NetworkCondition(50, 0, Instant.now()));
        assertFalse(monitor.isConnectionHealthy());
    }

    @Test
    void constructor_throwsOnNullProbe() {
        assertThrows(IllegalArgumentException.class, () -> new NetworkConditionMonitor(null));
    }
}
