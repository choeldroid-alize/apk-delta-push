package com.apkdeltapush.health;

import com.apkdeltapush.adb.AdbClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeviceHealthCheckerTest {

    private AdbClient adbClient;
    private DeviceHealthChecker checker;

    private static final String SERIAL = "emulator-5554";

    @BeforeEach
    void setUp() {
        adbClient = Mockito.mock(AdbClient.class);
        checker = new DeviceHealthChecker(adbClient);
    }

    @Test
    void constructor_nullAdbClient_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new DeviceHealthChecker(null));
    }

    @Test
    void checkHealth_nullSerial_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> checker.checkHealth(null));
    }

    @Test
    void checkHealth_blankSerial_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> checker.checkHealth("   "));
    }

    @Test
    void checkHealth_deviceNotConnected_returnsUnhealthyReport() {
        when(adbClient.isDeviceConnected(SERIAL)).thenReturn(false);

        DeviceHealthReport report = checker.checkHealth(SERIAL);

        assertFalse(report.isHealthy());
        assertTrue(report.getMessage().contains("not connected"));
        assertEquals("false", report.getMetrics().get("connected"));
    }

    @Test
    void checkHealth_lowBattery_returnsUnhealthyReport() {
        when(adbClient.isDeviceConnected(SERIAL)).thenReturn(true);
        when(adbClient.getBatteryLevel(SERIAL)).thenReturn(10);
        when(adbClient.getFreeStorageBytes(SERIAL)).thenReturn(200 * 1024 * 1024L);

        DeviceHealthReport report = checker.checkHealth(SERIAL);

        assertFalse(report.isHealthy());
        assertTrue(report.getMessage().contains("Battery too low"));
    }

    @Test
    void checkHealth_insufficientStorage_returnsUnhealthyReport() {
        when(adbClient.isDeviceConnected(SERIAL)).thenReturn(true);
        when(adbClient.getBatteryLevel(SERIAL)).thenReturn(80);
        when(adbClient.getFreeStorageBytes(SERIAL)).thenReturn(10 * 1024 * 1024L);

        DeviceHealthReport report = checker.checkHealth(SERIAL);

        assertFalse(report.isHealthy());
        assertTrue(report.getMessage().contains("Insufficient storage"));
    }

    @Test
    void checkHealth_allConditionsMet_returnsHealthyReport() {
        when(adbClient.isDeviceConnected(SERIAL)).thenReturn(true);
        when(adbClient.getBatteryLevel(SERIAL)).thenReturn(75);
        when(adbClient.getFreeStorageBytes(SERIAL)).thenReturn(500 * 1024 * 1024L);

        DeviceHealthReport report = checker.checkHealth(SERIAL);

        assertTrue(report.isHealthy());
        assertEquals("Device is healthy", report.getMessage());
        assertEquals(SERIAL, report.getDeviceSerial());
        assertNotNull(report.getTimestamp());
    }
}
