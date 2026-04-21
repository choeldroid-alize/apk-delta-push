package com.apkdeltapush.profile;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages lifecycle of {@link DevicePushProfile} instances, keyed by device ID.
 * Profiles are created on first access and can be updated as transfer metrics arrive.
 */
public class DevicePushProfileManager {

    private final Map<String, DevicePushProfile> profiles = new ConcurrentHashMap<>();

    /**
     * Returns the profile for the given device, creating a default one if absent.
     */
    public DevicePushProfile getOrCreate(String deviceId) {
        return profiles.computeIfAbsent(deviceId, DevicePushProfile::new);
    }

    /**
     * Returns an existing profile wrapped in Optional, or empty if not found.
     */
    public Optional<DevicePushProfile> find(String deviceId) {
        return Optional.ofNullable(profiles.get(deviceId));
    }

    /**
     * Registers or replaces a profile for the given device.
     */
    public void register(DevicePushProfile profile) {
        if (profile == null) throw new IllegalArgumentException("profile must not be null");
        profiles.put(profile.getDeviceId(), profile);
    }

    /**
     * Removes the profile for the given device.
     *
     * @return true if a profile was removed, false otherwise
     */
    public boolean remove(String deviceId) {
        return profiles.remove(deviceId) != null;
    }

    /**
     * Updates the recorded average transfer rate for a device based on a completed transfer.
     *
     * @param deviceId  target device
     * @param bytesTransferred bytes moved in the last transfer
     * @param durationMillis   wall-clock duration of the transfer
     */
    public void recordTransfer(String deviceId, long bytesTransferred, long durationMillis) {
        if (durationMillis <= 0) return;
        DevicePushProfile profile = getOrCreate(deviceId);
        long newRate = (bytesTransferred * 1000L) / durationMillis;
        long existing = profile.getAverageTransferRateBytesPerSec();
        // Exponential moving average (alpha = 0.3)
        long updated = existing == 0 ? newRate : (long) (0.3 * newRate + 0.7 * existing);
        profile.setAverageTransferRateBytesPerSec(updated);
    }

    /**
     * Returns an unmodifiable view of all registered profiles.
     */
    public Collection<DevicePushProfile> allProfiles() {
        return Collections.unmodifiableCollection(profiles.values());
    }

    /** Returns the number of tracked device profiles. */
    public int size() {
        return profiles.size();
    }
}
