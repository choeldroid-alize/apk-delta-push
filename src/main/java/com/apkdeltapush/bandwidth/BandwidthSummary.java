package com.apkdeltapush.bandwidth;

/**
 * Immutable summary of a bandwidth monitoring session.
 */
public class BandwidthSummary {

    private final long totalBytesTransferred;
    private final double averageRateBytesPerSecond;
    private final long startTimeMs;
    private final long endTimeMs;

    public BandwidthSummary(long totalBytesTransferred, double averageRateBytesPerSecond,
                             long startTimeMs, long endTimeMs) {
        this.totalBytesTransferred = totalBytesTransferred;
        this.averageRateBytesPerSecond = averageRateBytesPerSecond;
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
    }

    public long getTotalBytesTransferred() { return totalBytesTransferred; }
    public double getAverageRateBytesPerSecond() { return averageRateBytesPerSecond; }
    public long getStartTimeMs() { return startTimeMs; }
    public long getEndTimeMs() { return endTimeMs; }

    public long getElapsedMs() {
        return endTimeMs - startTimeMs;
    }

    public String getFormattedRate() {
        double kbps = averageRateBytesPerSecond / 1024.0;
        if (kbps >= 1024.0) {
            return String.format("%.2f MB/s", kbps / 1024.0);
        }
        return String.format("%.2f KB/s", kbps);
    }

    @Override
    public String toString() {
        return String.format("BandwidthSummary{bytes=%d, rate=%s, elapsedMs=%d}",
            totalBytesTransferred, getFormattedRate(), getElapsedMs());
    }
}
