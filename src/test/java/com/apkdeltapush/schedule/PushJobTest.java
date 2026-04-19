package com.apkdeltapush.schedule;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PushJobTest {

    @Test
    void initialStatus_isPending() {
        PushJob job = new PushJob("j1", "serial", "/apk", () -> {});
        assertEquals(JobStatus.PENDING, job.getStatus());
    }

    @Test
    void execute_setsCompletedOnSuccess() {
        PushJob job = new PushJob("j2", "serial", "/apk", () -> {});
        job.execute();
        assertEquals(JobStatus.COMPLETED, job.getStatus());
    }

    @Test
    void execute_setsFailedOnException() {
        PushJob job = new PushJob("j3", "serial", "/apk", () -> { throw new RuntimeException("boom"); });
        assertThrows(RuntimeException.class, job::execute);
        assertEquals(JobStatus.FAILED, job.getStatus());
    }

    @Test
    void constructor_nullJobId_throws() {
        assertThrows(NullPointerException.class, () -> new PushJob(null, "serial", "/apk", () -> {}));
    }

    @Test
    void toString_containsJobId() {
        PushJob job = new PushJob("j4", "serial", "/apk", () -> {});
        assertTrue(job.toString().contains("j4"));
    }
}
