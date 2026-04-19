package com.apkdeltapush.schedule;

import org.junit.jupiter.api.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class PushSchedulerTest {

    private PushScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new PushScheduler(2);
    }

    @AfterEach
    void tearDown() {
        scheduler.shutdown();
    }

    @Test
    void scheduleNow_executesTask() throws InterruptedException {
        AtomicBoolean ran = new AtomicBoolean(false);
        PushJob job = new PushJob("job-1", "emulator-5554", "/path/app.apk", () -> ran.set(true));
        scheduler.scheduleNow(job);
        Thread.sleep(300);
        assertTrue(ran.get());
    }

    @Test
    void schedule_withDelay_runsAfterDelay() throws InterruptedException {
        AtomicBoolean ran = new AtomicBoolean(false);
        PushJob job = new PushJob("job-2", "emulator-5554", "/path/app.apk", () -> ran.set(true));
        scheduler.schedule(job, 1);
        assertFalse(ran.get());
        Thread.sleep(1500);
        assertTrue(ran.get());
    }

    @Test
    void cancel_preventsPendingJobFromRunning() throws InterruptedException {
        AtomicBoolean ran = new AtomicBoolean(false);
        PushJob job = new PushJob("job-3", "emulator-5554", "/path/app.apk", () -> ran.set(true));
        scheduler.schedule(job, 5);
        assertTrue(scheduler.isScheduled("job-3"));
        boolean cancelled = scheduler.cancel("job-3");
        assertTrue(cancelled);
        assertFalse(scheduler.isScheduled("job-3"));
        Thread.sleep(300);
        assertFalse(ran.get());
    }

    @Test
    void duplicateSchedule_isIgnored() {
        PushJob job = new PushJob("job-4", "emulator-5554", "/path/app.apk", () -> {});
        scheduler.schedule(job, 10);
        scheduler.schedule(job, 10); // second call should be ignored without exception
        assertTrue(scheduler.isScheduled("job-4"));
    }

    @Test
    void cancel_nonExistentJob_returnsFalse() {
        assertFalse(scheduler.cancel("no-such-job"));
    }
}
