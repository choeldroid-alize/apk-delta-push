package com.apkdeltapush.priority;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Manages a priority-ordered queue of APK push jobs,
 * allowing higher-priority pushes to preempt lower ones.
 */
public class PushPriorityQueue {

    private final PriorityQueue<PrioritizedPushJob> queue;

    public PushPriorityQueue() {
        this.queue = new PriorityQueue<>(
            Comparator.comparingInt(PrioritizedPushJob::getPriority).reversed()
        );
    }

    /**
     * Enqueues a push job with the given priority.
     *
     * @param job the push job to enqueue
     */
    public void enqueue(PrioritizedPushJob job) {
        if (job == null) {
            throw new IllegalArgumentException("Push job must not be null");
        }
        queue.offer(job);
    }

    /**
     * Polls and returns the highest-priority job, or empty if the queue is empty.
     */
    public Optional<PrioritizedPushJob> poll() {
        return Optional.ofNullable(queue.poll());
    }

    /**
     * Peeks at the highest-priority job without removing it.
     */
    public Optional<PrioritizedPushJob> peek() {
        return Optional.ofNullable(queue.peek());
    }

    /**
     * Returns a snapshot of all queued jobs ordered by priority (highest first).
     */
    public List<PrioritizedPushJob> snapshot() {
        List<PrioritizedPushJob> sorted = new ArrayList<>(queue);
        sorted.sort(Comparator.comparingInt(PrioritizedPushJob::getPriority).reversed());
        return sorted;
    }

    /**
     * Removes a specific job from the queue by its job ID.
     *
     * @param jobId the ID of the job to remove
     * @return true if the job was found and removed
     */
    public boolean remove(String jobId) {
        return queue.removeIf(job -> job.getJobId().equals(jobId));
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void clear() {
        queue.clear();
    }
}
