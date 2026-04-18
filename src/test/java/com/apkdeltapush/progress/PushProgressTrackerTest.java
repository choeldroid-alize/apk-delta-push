package com.apkdeltapush.progress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PushProgressTrackerTest {

    private List<Integer> percents;
    private List<PushPhase> phases;
    private boolean completed;
    private String failureReason;

    private ProgressListener captureListener;

    @BeforeEach
    void setUp() {
        percents = new ArrayList<>();
        phases = new ArrayList<>();
        completed = false;
        failureReason = null;

        captureListener = new ProgressListener() {
            @Override
            public void onProgress(PushPhase phase, int pct, long transferred, long total) {
                percents.add(pct);
            }
            @Override
            public void onPhaseChanged(PushPhase newPhase) {
                phases.add(newPhase);
            }
            @Override
            public void onComplete() { completed = true; }
            @Override
            public void onFailure(String reason) { failureReason = reason; }
        };
    }

    @Test
    void advanceUpdatesTransferredBytes() {
        PushProgressTracker tracker = new PushProgressTracker(1000, captureListener);
        tracker.advance(250);
        assertEquals(250, tracker.getTransferredBytes());
        assertFalse(percents.isEmpty());
        assertEquals(25, percents.get(0));
    }

    @Test
    void setPhaseNotifiesListener() {
        PushProgressTracker tracker = new PushProgressTracker(500, captureListener);
        tracker.setPhase(PushPhase.TRANSFERRING);
        assertEquals(PushPhase.TRANSFERRING, tracker.getCurrentPhase());
        assertTrue(phases.contains(PushPhase.TRANSFERRING));
    }

    @Test
    void completeSignalsListener() {
        PushProgressTracker tracker = new PushProgressTracker(100, captureListener);
        tracker.complete();
        assertTrue(completed);
        assertEquals(100, tracker.getTransferredBytes());
    }

    @Test
    void failSignalsListener() {
        PushProgressTracker tracker = new PushProgressTracker(100, captureListener);
        tracker.fail("connection lost");
        assertEquals("connection lost", failureReason);
    }

    @Test
    void invalidTotalBytesThrows() {
        assertThrows(IllegalArgumentException.class, () -> new PushProgressTracker(0, captureListener));
    }
}
