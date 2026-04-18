package com.apkdeltapush.progress;

/**
 * A simple console-based progress listener that prints updates to stdout.
 */
public class ConsoleProgressListener implements ProgressListener {

    private static final int BAR_WIDTH = 30;
    private int lastPrintedPercent = -1;

    @Override
    public void onProgress(PushPhase phase, int percentComplete, long bytesTransferred, long totalBytes) {
        if (percentComplete == lastPrintedPercent) return;
        lastPrintedPercent = percentComplete;

        int filled = (BAR_WIDTH * percentComplete) / 100;
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < BAR_WIDTH; i++) {
            bar.append(i < filled ? '=' : ' ');
        }
        bar.append("]");

        System.out.printf("\r%-20s %s %3d%% (%s / %s)",
                phase.getDisplayName(),
                bar,
                percentComplete,
                formatBytes(bytesTransferred),
                formatBytes(totalBytes));
        System.out.flush();
    }

    @Override
    public void onPhaseChanged(PushPhase newPhase) {
        System.out.printf("%n[Phase] -> %s%n", newPhase.getDisplayName());
        lastPrintedPercent = -1;
    }

    @Override
    public void onComplete() {
        System.out.println("\n[Done] Push completed successfully.");
    }

    @Override
    public void onFailure(String reason) {
        System.out.printf("%n[Error] Push failed: %s%n", reason);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024));
    }
}
