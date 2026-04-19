package com.apkdeltapush.log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PushEventLogger {

    private final Path logFile;
    private final List<PushEvent> inMemoryLog = new ArrayList<>();
    private final boolean persistToDisk;

    public PushEventLogger(Path logFile, boolean persistToDisk) {
        this.logFile = logFile;
        this.persistToDisk = persistToDisk;
    }

    public void log(PushEventType type, String deviceSerial, String message) {
        PushEvent event = new PushEvent(type, deviceSerial, message, Instant.now());
        inMemoryLog.add(event);
        if (persistToDisk) {
            writeToFile(event);
        }
    }

    private void writeToFile(PushEvent event) {
        String line = event.getTimestamp() + " [" + event.getType() + "] "
                + event.getDeviceSerial() + " - " + event.getMessage() + System.lineSeparator();
        try {
            if (logFile.getParent() != null) {
                Files.createDirectories(logFile.getParent());
            }
            Files.writeString(logFile, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write push event log: " + e.getMessage());
        }
    }

    public List<PushEvent> getEvents() {
        return Collections.unmodifiableList(inMemoryLog);
    }

    public List<PushEvent> getEventsByDevice(String deviceSerial) {
        List<PushEvent> result = new ArrayList<>();
        for (PushEvent e : inMemoryLog) {
            if (e.getDeviceSerial().equals(deviceSerial)) {
                result.add(e);
            }
        }
        return result;
    }

    public void clear() {
        inMemoryLog.clear();
    }
}
