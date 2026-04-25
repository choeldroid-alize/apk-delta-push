package com.apkdeltapush.mirror;

import java.util.*;

/**
 * Aggregated outcome of a mirror push operation across all target devices.
 */
public class DeviceMirrorResult {

    private final String sourceDeviceId;
    private final Map<String, Boolean> deviceOutcomes;

    private DeviceMirrorResult(String sourceDeviceId, Map<String, Boolean> deviceOutcomes) {
        this.sourceDeviceId = sourceDeviceId;
        this.deviceOutcomes = Collections.unmodifiableMap(new LinkedHashMap<>(deviceOutcomes));
    }

    public static DeviceMirrorResult of(String sourceDeviceId, Map<String, Boolean> outcomes) {
        return new DeviceMirrorResult(sourceDeviceId, outcomes);
    }

    public static DeviceMirrorResult empty(String sourceDeviceId) {
        return new DeviceMirrorResult(sourceDeviceId, Collections.emptyMap());
    }

    public String getSourceDeviceId() { return sourceDeviceId; }

    public Map<String, Boolean> getDeviceOutcomes() { return deviceOutcomes; }

    public boolean isFullSuccess() {
        return !deviceOutcomes.isEmpty() && deviceOutcomes.values().stream().allMatch(Boolean.TRUE::equals);
    }

    public boolean isPartialSuccess() {
        long successes = deviceOutcomes.values().stream().filter(Boolean.TRUE::equals).count();
        return successes > 0 && successes < deviceOutcomes.size();
    }

    public List<String> getFailedDevices() {
        List<String> failed = new ArrayList<>();
        deviceOutcomes.forEach((id, ok) -> { if (!ok) failed.add(id); });
        return Collections.unmodifiableList(failed);
    }

    public int totalTargets() { return deviceOutcomes.size(); }

    public int successCount() {
        return (int) deviceOutcomes.values().stream().filter(Boolean.TRUE::equals).count();
    }

    @Override
    public String toString() {
        return "DeviceMirrorResult{source=" + sourceDeviceId +
               ", total=" + totalTargets() +
               ", success=" + successCount() +
               ", failed=" + getFailedDevices() + "}";
    }
}
