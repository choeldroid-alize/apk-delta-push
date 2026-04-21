package com.apkdeltapush.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DevicePushProfileManagerTest {

    private DevicePushProfileManager manager;

    @BeforeEach
    void setUp() {
        manager = new DevicePushProfileManager();
    }

    @Test
    void getOrCreate_createsNewProfile() {
        DevicePushProfile profile = manager.getOrCreate("dev-1");
        assertNotNull(profile);
        assertEquals("dev-1", profile.getDeviceId());
        assertEquals(1, manager.size());
    }

    @Test
    void getOrCreate_returnsSameInstanceOnSecondCall() {
        DevicePushProfile p1 = manager.getOrCreate("dev-2");
        DevicePushProfile p2 = manager.getOrCreate("dev-2");
        assertSame(p1, p2);
        assertEquals(1, manager.size());
    }

    @Test
    void find_returnsEmptyWhenAbsent() {
        Optional<DevicePushProfile> result = manager.find("unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    void find_returnsPresentWhenExists() {
        manager.getOrCreate("dev-3");
        Optional<DevicePushProfile> result = manager.find("dev-3");
        assertTrue(result.isPresent());
    }

    @Test
    void register_replacesExistingProfile() {
        manager.getOrCreate("dev-4");
        DevicePushProfile replacement = new DevicePushProfile("dev-4");
        replacement.setCompressionEnabled(false);
        manager.register(replacement);
        assertSame(replacement, manager.find("dev-4").orElseThrow());
        assertFalse(manager.find("dev-4").orElseThrow().isCompressionEnabled());
    }

    @Test
    void register_nullThrows() {
        assertThrows(IllegalArgumentException.class, () -> manager.register(null));
    }

    @Test
    void remove_existingProfile() {
        manager.getOrCreate("dev-5");
        assertTrue(manager.remove("dev-5"));
        assertEquals(0, manager.size());
    }

    @Test
    void remove_nonExistentReturnsFalse() {
        assertFalse(manager.remove("ghost"));
    }

    @Test
    void recordTransfer_updatesAverageRate() {
        manager.getOrCreate("dev-6");
        // 1 MB in 1000 ms => 1024 bytes/ms => 1 048 576 bytes/s
        manager.recordTransfer("dev-6", 1024 * 1024, 1000);
        long rate = manager.find("dev-6").orElseThrow().getAverageTransferRateBytesPerSec();
        assertEquals(1024 * 1024, rate);
    }

    @Test
    void recordTransfer_zeroDurationIsIgnored() {
        manager.getOrCreate("dev-7");
        manager.recordTransfer("dev-7", 500_000, 0);
        assertEquals(0L, manager.find("dev-7").orElseThrow().getAverageTransferRateBytesPerSec());
    }

    @Test
    void allProfiles_isUnmodifiable() {
        manager.getOrCreate("dev-8");
        assertThrows(UnsupportedOperationException.class,
                () -> manager.allProfiles().clear());
    }
}
