package com.apkdeltapush.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DevicePushFilterTest {

    private DeviceFilterContext device;

    @BeforeEach
    void setUp() {
        device = mock(DeviceFilterContext.class);
        when(device.getApiLevel()).thenReturn(30);
        when(device.getBatteryPercent()).thenReturn(80);
        when(device.isOnWifi()).thenReturn(true);
        when(device.getSerial()).thenReturn("emulator-5554");
    }

    @Test
    void acceptsDeviceMeetingAllCriteria() {
        DevicePushFilter filter = new DevicePushFilter()
                .withMinApiLevel(21)
                .withMaxApiLevel(33)
                .withMinBatteryPercent(50)
                .requireWifi(true);
        assertTrue(filter.accepts(device));
    }

    @Test
    void rejectsDeviceBelowMinApi() {
        DevicePushFilter filter = new DevicePushFilter().withMinApiLevel(31);
        assertFalse(filter.accepts(device));
    }

    @Test
    void rejectsDeviceLowBattery() {
        when(device.getBatteryPercent()).thenReturn(20);
        DevicePushFilter filter = new DevicePushFilter().withMinBatteryPercent(50);
        assertFalse(filter.accepts(device));
    }

    @Test
    void rejectsDeviceNotOnWifi() {
        when(device.isOnWifi()).thenReturn(false);
        DevicePushFilter filter = new DevicePushFilter().requireWifi(true);
        assertFalse(filter.accepts(device));
    }

    @Test
    void filtersListCorrectly() {
        DeviceFilterContext other = mock(DeviceFilterContext.class);
        when(other.getApiLevel()).thenReturn(19);
        when(other.getBatteryPercent()).thenReturn(90);
        when(other.isOnWifi()).thenReturn(true);
        when(other.getSerial()).thenReturn("device-001");

        DevicePushFilter filter = new DevicePushFilter().withMinApiLevel(21);
        List<DeviceFilterContext> result = filter.filter(List.of(device, other));
        assertEquals(1, result.size());
        assertSame(device, result.get(0));
    }

    @Test
    void acceptsNullReturnsFalse() {
        DevicePushFilter filter = new DevicePushFilter();
        assertFalse(filter.accepts(null));
    }

    @Test
    void predicateCountReflectsAddedFilters() {
        DevicePushFilter filter = new DevicePushFilter()
                .withMinApiLevel(21)
                .withMinBatteryPercent(30);
        assertEquals(2, filter.predicateCount());
    }
}
