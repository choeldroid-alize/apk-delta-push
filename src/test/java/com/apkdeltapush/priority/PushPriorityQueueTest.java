package com.apkdeltapush.priority;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PushPriorityQueueTest {

    private PushPriorityQueue queue;

    @BeforeEach
    void setUp() {
        queue = new PushPriorityQueue();
    }

    @Test
    void testEnqueueAndPollReturnsHighestPriorityFirst() {
        queue.enqueue(new PrioritizedPushJob("job-1", "device-A", "/apk/low.apk", PrioritizedPushJob.PRIORITY_LOW));
        queue.enqueue(new PrioritizedPushJob("job-2", "device-B", "/apk/high.apk", PrioritizedPushJob.PRIORITY_HIGH));
        queue.enqueue(new PrioritizedPushJob("job-3", "device-C", "/apk/normal.apk", PrioritizedPushJob.PRIORITY_NORMAL));

        Optional<PrioritizedPushJob> first = queue.poll();
        assertTrue(first.isPresent());
        assertEquals("job-2", first.get().getJobId());

        Optional<PrioritizedPushJob> second = queue.poll();
        assertTrue(second.isPresent());
        assertEquals("job-3", second.get().getJobId());
    }

    @Test
    void testPollOnEmptyQueueReturnsEmpty() {
        assertTrue(queue.poll().isEmpty());
    }

    @Test
    void testPeekDoesNotRemoveJob() {
        queue.enqueue(new PrioritizedPushJob("job-1", "device-A", "/apk/app.apk", PrioritizedPushJob.PRIORITY_CRITICAL));
        queue.peek();
        assertEquals(1, queue.size());
    }

    @Test
    void testRemoveByJobId() {
        queue.enqueue(new PrioritizedPushJob("job-1", "device-A", "/apk/app.apk", PrioritizedPushJob.PRIORITY_NORMAL));
        queue.enqueue(new PrioritizedPushJob("job-2", "device-B", "/apk/app2.apk", PrioritizedPushJob.PRIORITY_HIGH));

        boolean removed = queue.remove("job-1");
        assertTrue(removed);
        assertEquals(1, queue.size());
    }

    @Test
    void testRemoveNonExistentJobReturnsFalse() {
        queue.enqueue(new PrioritizedPushJob("job-1", "device-A", "/apk/app.apk", PrioritizedPushJob.PRIORITY_LOW));
        assertFalse(queue.remove("job-99"));
    }

    @Test
    void testSnapshotReturnsSortedList() {
        queue.enqueue(new PrioritizedPushJob("job-1", "device-A", "/apk/a.apk", PrioritizedPushJob.PRIORITY_LOW));
        queue.enqueue(new PrioritizedPushJob("job-2", "device-B", "/apk/b.apk", PrioritizedPushJob.PRIORITY_CRITICAL));
        queue.enqueue(new PrioritizedPushJob("job-3", "device-C", "/apk/c.apk", PrioritizedPushJob.PRIORITY_NORMAL));

        List<PrioritizedPushJob> snapshot = queue.snapshot();
        assertEquals(3, snapshot.size());
        assertEquals(PrioritizedPushJob.PRIORITY_CRITICAL, snapshot.get(0).getPriority());
        assertEquals(PrioritizedPushJob.PRIORITY_LOW, snapshot.get(2).getPriority());
    }

    @Test
    void testEnqueueNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> queue.enqueue(null));
    }

    @Test
    void testClearEmptiesQueue() {
        queue.enqueue(new PrioritizedPushJob("job-1", "device-A", "/apk/app.apk", PrioritizedPushJob.PRIORITY_HIGH));
        queue.clear();
        assertTrue(queue.isEmpty());
    }
}
