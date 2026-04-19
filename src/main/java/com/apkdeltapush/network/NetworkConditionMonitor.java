package com.apkdeltapush.network;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Monitors current network conditions between host and connected device,
 * providing latency and throughput estimates to guide transfer decisions.
 */
public class NetworkConditionMonitor {

    private static final long STALE_THRESHOLD_MS = 5_000;

    private final AtomicReference<NetworkCondition> lastCondition = new AtomicReference<>();
    private final NetworkProbe probe;

    public NetworkConditionMonitor(NetworkProbe probe) {
        if (probe == null) throw new IllegalArgumentException("probe must not be null");
        this.probe = probe;
    }

    /**
     * Returns the current network condition, refreshing if the cached value is stale.
     */
    public NetworkCondition getCurrentCondition() {
        NetworkCondition cached = lastCondition.get();
        if (cached == null || isStale(cached)) {
            NetworkCondition fresh = probe.measure();
            lastCondition.set(fresh);
            return fresh;
        }
        return cached;
    }

    /**
     * Forces a fresh measurement regardless of cache state.
     */
    public NetworkCondition refresh() {
        NetworkCondition fresh = probe.measure();
        lastCondition.set(fresh);
        return fresh;
    }

    public boolean isConnectionHealthy() {
        NetworkCondition c = getCurrentCondition();
        return c.getLatencyMs() < 500 && c.getThroughputBytesPerSec() > 0;
    }

    private boolean isStale(NetworkCondition condition) {
        return Instant.now().toEpochMilli() - condition.getMeasuredAt().toEpochMilli() > STALE_THRESHOLD_MS;
    }
}
