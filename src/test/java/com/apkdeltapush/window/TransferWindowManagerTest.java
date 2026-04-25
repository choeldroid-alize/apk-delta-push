package com.apkdeltapush.window;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class TransferWindowManagerTest {

    private TransferWindowConfig config;
    private TransferWindowManager manager;

    @BeforeEach
    void setUp() {
        config = TransferWindowConfig.builder()
            .maxWindowSize(8)
            .initialWindowSize(2)
            .ackTimeout(Duration.ofMillis(200))
            .adaptiveScaling(true)
            .scaleStepSize(2)
            .build();
        manager = new TransferWindowManager(config);
    }

    @Test
    void acquireAndReleaseWithinInitialWindow() throws InterruptedException {
        assertTrue(manager.acquireSlot());
        assertTrue(manager.acquireSlot());
        manager.acknowledgeChunk();
        manager.acknowledgeChunk();
        assertEquals(2, manager.getCurrentWindowSize());
    }

    @Test
    void acquireTimesOutWhenWindowFull() throws InterruptedException {
        manager.acquireSlot();
        manager.acquireSlot();
        // window is now exhausted; next acquire should time out
        boolean result = manager.acquireSlot();
        assertFalse(result);
    }

    @Test
    void windowExpandsAfterConsecutiveAcks() throws InterruptedException {
        manager.acquireSlot();
        manager.acknowledgeChunk(); // ack 1
        manager.acquireSlot();
        manager.acknowledgeChunk(); // ack 2 — triggers expand (step=2)
        assertEquals(4, manager.getCurrentWindowSize());
    }

    @Test
    void windowShrinksOnNack() throws InterruptedException {
        // expand first so shrink has room
        manager.acquireSlot();
        manager.acknowledgeChunk();
        manager.acquireSlot();
        manager.acknowledgeChunk(); // window now 4

        manager.acquireSlot();
        manager.negativeAcknowledgeChunk(); // shrink back
        assertTrue(manager.getCurrentWindowSize() < 4);
    }

    @Test
    void windowDoesNotExceedMax() throws InterruptedException {
        // drive many acks to try to exceed max
        for (int i = 0; i < 20; i++) {
            if (manager.acquireSlot()) {
                manager.acknowledgeChunk();
            }
        }
        assertTrue(manager.getCurrentWindowSize() <= config.getMaxWindowSize());
    }

    @Test
    void windowDoesNotShrinkBelowOne() throws InterruptedException {
        // drain window and nack repeatedly
        for (int i = 0; i < 10; i++) {
            manager.acquireSlot();
            manager.negativeAcknowledgeChunk();
        }
        assertTrue(manager.getCurrentWindowSize() >= 1);
    }

    @Test
    void resetRestoresInitialState() throws InterruptedException {
        manager.acquireSlot();
        manager.acknowledgeChunk();
        manager.acquireSlot();
        manager.acknowledgeChunk(); // expanded

        manager.reset();
        assertEquals(config.getInitialWindowSize(), manager.getCurrentWindowSize());
    }

    @Test
    void adaptiveScalingDisabledDoesNotChangeWindowSize() throws InterruptedException {
        TransferWindowConfig staticCfg = TransferWindowConfig.builder()
            .maxWindowSize(8)
            .initialWindowSize(4)
            .ackTimeout(Duration.ofMillis(200))
            .adaptiveScaling(false)
            .build();
        TransferWindowManager staticMgr = new TransferWindowManager(staticCfg);

        for (int i = 0; i < 6; i++) {
            if (staticMgr.acquireSlot()) {
                staticMgr.acknowledgeChunk();
            }
        }
        assertEquals(4, staticMgr.getCurrentWindowSize());
    }
}
