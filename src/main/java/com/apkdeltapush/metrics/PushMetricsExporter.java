package com.apkdeltapush.metrics;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Exports a {@link PushMetricsSnapshot} to various formats.
 */
public class PushMetricsExporter {

    /**
     * Writes the snapshot as a human-readable text report.
     */
    public void exportText(PushMetricsSnapshot snapshot, Writer writer) throws IOException {
        writer.write("=== Push Metrics Report ===\n");
        writer.write("Session   : " + snapshot.getSessionId() + "\n");
        writer.write("Captured  : " + snapshot.getCapturedAt() + "\n");
        writer.write("Sent      : " + snapshot.getTotalBytesSent() + " bytes\n");
        writer.write("Received  : " + snapshot.getTotalBytesReceived() + " bytes\n");
        writer.write("Successes : " + snapshot.getSuccessfulPushes() + "\n");
        writer.write("Failures  : " + snapshot.getFailedPushes() + "\n");
        writer.write(String.format("Rate      : %.2f Bps%n", snapshot.getAverageTransferRateBps()));
        writer.write(String.format("Success%%  : %.1f%%%n", snapshot.getSuccessRate()));
        writer.write("--- Per Device ---\n");
        for (Map.Entry<String, Long> entry : snapshot.getPerDeviceBytesSent().entrySet()) {
            writer.write(String.format("  %s : %d bytes%n", entry.getKey(), entry.getValue()));
        }
        writer.flush();
    }

    /**
     * Writes the snapshot as a minimal JSON object.
     */
    public void exportJson(PushMetricsSnapshot snapshot, Writer writer) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"sessionId\": \"").append(escape(snapshot.getSessionId())).append("\",\n");
        sb.append("  \"capturedAt\": \"").append(snapshot.getCapturedAt()).append("\",\n");
        sb.append("  \"totalBytesSent\": ").append(snapshot.getTotalBytesSent()).append(",\n");
        sb.append("  \"totalBytesReceived\": ").append(snapshot.getTotalBytesReceived()).append(",\n");
        sb.append("  \"successfulPushes\": ").append(snapshot.getSuccessfulPushes()).append(",\n");
        sb.append("  \"failedPushes\": ").append(snapshot.getFailedPushes()).append(",\n");
        sb.append(String.format("  \"averageTransferRateBps\": %.4f,%n", snapshot.getAverageTransferRateBps()));
        sb.append(String.format("  \"successRate\": %.4f,%n", snapshot.getSuccessRate()));
        sb.append("  \"perDeviceBytesSent\": {\n");
        Map<String, Long> dev = snapshot.getPerDeviceBytesSent();
        int i = 0;
        for (Map.Entry<String, Long> e : dev.entrySet()) {
            sb.append("    \"").append(escape(e.getKey())).append("\": ").append(e.getValue());
            if (++i < dev.size()) sb.append(",");
            sb.append("\n");
        }
        sb.append("  }\n}\n");
        writer.write(sb.toString());
        writer.flush();
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
