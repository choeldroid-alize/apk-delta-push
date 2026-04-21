package com.apkdeltapush.parallel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParallelPushCoordinatorTest {

    private ParallelPushCoordinator coordinator;

    @BeforeEach
    void setUp() {
        coordinator = new ParallelPushCoordinator(3);
    }

    @AfterEach
    void tearDown() {
        coordinator.shutdown();
    }

    @Test
    void constructor_invalidParallelism_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new ParallelPushCoordinator(0));
        assertThrows(IllegalArgumentException.class, () -> new ParallelPushCoordinator(-1));
    }

    @Test
    void pushToAll_emptyList_returnsEmptyMap() throws InterruptedException {
        Map<String, ParallelPushResult> results = coordinator.pushToAll(List.of(), deviceId ->
                () -> ParallelPushResult.success(deviceId, 10L));
        assertTrue(results.isEmpty());
    }

    @Test
    void pushToAll_allSucceed_returnsSuccessResults() throws InterruptedException {
        List<String> devices = List.of("device1", "device2", "device3");
        PushTaskFactory factory = deviceId -> () -> ParallelPushResult.success(deviceId, 50L);

        Map<String, ParallelPushResult> results = coordinator.pushToAll(devices, factory);

        assertEquals(3, results.size());
        for (String deviceId : devices) {
            assertTrue(results.containsKey(deviceId));
            assertTrue(results.get(deviceId).isSuccess());
            assertEquals(50L, results.get(deviceId).getDurationMs());
        }
    }

    @Test
    void pushToAll_taskThrowsException_returnsFailureResult() throws InterruptedException {
        List<String> devices = List.of("failDevice");
        PushTaskFactory factory = deviceId -> () -> {
            throw new RuntimeException("ADB connection lost");
        };

        Map<String, ParallelPushResult> results = coordinator.pushToAll(devices, factory);

        assertEquals(1, results.size());
        ParallelPushResult result = results.get("failDevice");
        assertFalse(result.isSuccess());
        assertEquals("ADB connection lost", result.getErrorMessage());
    }

    @Test
    void pushToAll_mixedResults_handlesPartialFailure() throws InterruptedException {
        List<String> devices = List.of("ok", "fail");
        PushTaskFactory factory = deviceId -> () -> {
            if (deviceId.equals("fail")) {
                throw new RuntimeException("timeout");
            }
            return ParallelPushResult.success(deviceId, 20L);
        };

        Map<String, ParallelPushResult> results = coordinator.pushToAll(devices, factory);

        assertTrue(results.get("ok").isSuccess());
        assertFalse(results.get("fail").isSuccess());
        assertEquals("timeout", results.get("fail").getErrorMessage());
    }

    @Test
    void getMaxParallelism_returnsConfiguredValue() {
        assertEquals(3, coordinator.getMaxParallelism());
    }

    @Test
    void shutdown_marksCoordinatorAsShutdown() {
        coordinator.shutdown();
        assertTrue(coordinator.isShutdown());
    }
}
