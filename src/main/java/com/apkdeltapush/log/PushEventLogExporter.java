package com.apkdeltapush.log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class PushEventLogExporter {

    private final PushEventLogger logger;

    public PushEventLogExporter(PushEventLogger logger) {
        this.logger = logger;
    }

    public void exportToCsv(Path outputPath) throws IOException {
        List<String> lines = new java.util.ArrayList<>();
        lines.add("timestamp,type,device,message");
        for (PushEvent e : logger.getEvents()) {
            lines.add(String.join(",",
                    e.getTimestamp().toString(),
                    e.getType().name(),
                    e.getDeviceSerial(),
                    escapeCSV(e.getMessage())));
        }
        Files.write(outputPath, lines);
    }

    public String exportToText() {
        return logger.getEvents().stream()
                .map(PushEvent::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public List<PushEvent> filterByType(PushEventType type) {
        return logger.getEvents().stream()
                .filter(e -> e.getType() == type)
                .collect(Collectors.toList());
    }

    private String escapeCSV(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
