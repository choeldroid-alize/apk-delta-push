package com.apkdeltapush.device;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains a registry of connected Android devices discovered via ADB.
 */
public class DeviceRegistry {

    private final Map<String, DeviceInfo> devices = new ConcurrentHashMap<>();

    /**
     * Registers or updates a device entry.
     *
     * @param info the device info to register
     */
    public void register(DeviceInfo info) {
        if (info == null || info.getSerialNumber() == null) {
            throw new IllegalArgumentException("DeviceInfo and serial number must not be null");
        }
        devices.put(info.getSerialNumber(), info);
    }

    /**
     * Removes a device from the registry by serial number.
     *
     * @param serialNumber the serial number of the device to remove
     * @return true if the device was present and removed
     */
    public boolean unregister(String serialNumber) {
        return devices.remove(serialNumber) != null;
    }

    /**
     * Returns the DeviceInfo for the given serial number, or empty if not found.
     */
    public Optional<DeviceInfo> find(String serialNumber) {
        return Optional.ofNullable(devices.get(serialNumber));
    }

    /**
     * Returns an unmodifiable view of all registered devices.
     */
    public Collection<DeviceInfo> listAll() {
        return Collections.unmodifiableCollection(devices.values());
    }

    /**
     * Returns the number of currently registered devices.
     */
    public int size() {
        return devices.size();
    }

    /**
     * Clears all registered devices.
     */
    public void clear() {
        devices.clear();
    }
}
