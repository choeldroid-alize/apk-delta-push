package com.apkdeltapush.device;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeviceRegistryTest {

    private DeviceRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DeviceRegistry();
    }

    @Test
    void testRegisterAndFind() {
        DeviceInfo info = new DeviceInfo("emulator-5554", "Pixel 6", "sdk_gphone64_x86_64");
        registry.register(info);

        Optional<DeviceInfo> found = registry.find("emulator-5554");
        assertTrue(found.isPresent());
        assertEquals("Pixel 6", found.get().getModel());
    }

    @Test
    void testRegisterNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> registry.register(null));
    }

    @Test
    void testRegisterNullSerialThrows() {
        DeviceInfo info = new DeviceInfo(null, "Pixel", "generic");
        assertThrows(IllegalArgumentException.class, () -> registry.register(info));
    }

    @Test
    void testUnregister() {
        DeviceInfo info = new DeviceInfo("emulator-5554", "Pixel 6", "sdk");
        registry.register(info);
        assertTrue(registry.unregister("emulator-5554"));
        assertFalse(registry.find("emulator-5554").isPresent());
    }

    @Test
    void testUnregisterNonExistent() {
        assertFalse(registry.unregister("nonexistent"));
    }

    @Test
    void testListAll() {
        registry.register(new DeviceInfo("serial1", "Model A", "product1"));
        registry.register(new DeviceInfo("serial2", "Model B", "product2"));

        Collection<DeviceInfo> all = registry.listAll();
        assertEquals(2, all.size());
    }

    @Test
    void testSize() {
        assertEquals(0, registry.size());
        registry.register(new DeviceInfo("serial1", "Model A", "product1"));
        assertEquals(1, registry.size());
    }

    @Test
    void testClear() {
        registry.register(new DeviceInfo("serial1", "Model A", "product1"));
        registry.clear();
        assertEquals(0, registry.size());
    }

    @Test
    void testRegisterOverwritesExisting() {
        registry.register(new DeviceInfo("serial1", "Old Model", "product"));
        registry.register(new DeviceInfo("serial1", "New Model", "product"));
        assertEquals("New Model", registry.find("serial1").get().getModel());
        assertEquals(1, registry.size());
    }
}
