package com.apkdeltapush.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BatchPushManagerTest {

    private BatchPushManager manager;

    @BeforeEach
    void setUp() {
        manager = new BatchPushManager();
    }

    private BatchPushJob makeJob(String id) {
        return new BatchPushJob(id, "emulator-" + id, "/tmp/app.apk", 1);
    }

    @Test
    void addJob_increasesJobCount() {
        manager.addJob(makeJob("j1"));
        manager.addJob(makeJob("j2"));
        assertEquals(2, manager.getPendingJobs().size());
    }

    @Test
    void addJob_nullThrows() {
        assertThrows(IllegalArgumentException.class, () -> manager.addJob(null));
    }

    @Test
    void clearJobs_emptiesList() {
        manager.addJob(makeJob("j1"));
        manager.clearJobs();
        assertTrue(manager.getPendingJobs().isEmpty());
    }

    @Test
    void executeBatch_allSuccess() {
        manager.addJob(makeJob("j1"));
        manager.addJob(makeJob("j2"));
        manager.executeBatch(job -> BatchPushResult.ok(job, 100L));

        List<BatchPushResult> results = manager.getCompletedResults();
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(BatchPushResult::isSuccess));
    }

    @Test
    void executeBatch_partialFailure_recordedCorrectly() {
        manager.addJob(makeJob("j1"));
        manager.addJob(makeJob("j2"));
        manager.executeBatch(job -> {
            if (job.getJobId().equals("j1")) return BatchPushResult.ok(job, 50L);
            return BatchPushResult.failure(job, "timeout", 200L);
        });

        BatchSummary summary = manager.summarize();
        assertEquals(2, summary.getTotal());
        assertEquals(1, summary.getSuccessCount());
        assertEquals(1, summary.getFailureCount());
        assertFalse(summary.isFullySuccessful());
    }

    @Test
    void summarize_emptyResults_zeroRate() {
        BatchSummary summary = manager.summarize();
        assertEquals(0.0, summary.getSuccessRate());
    }

    @Test
    void executeBatch_nullExecutorThrows() {
        assertThrows(IllegalArgumentException.class, () -> manager.executeBatch(null));
    }

    @Test
    void summarize_fullySuccessful() {
        manager.addJob(makeJob("j1"));
        manager.executeBatch(job -> BatchPushResult.ok(job, 10L));
        assertTrue(manager.summarize().isFullySuccessful());
    }
}
