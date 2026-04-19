package com.apkdeltapush.staging;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages temporary staging areas for delta patches before they are pushed to devices.
 * Each staging area is identified by a unique session ID and holds the patch file
 * along with metadata needed during the push lifecycle.
 */
public class StagingAreaManager {

    private final Path stagingRoot;
    private final Map<String, StagingArea> activeStagingAreas = new HashMap<>();

    public StagingAreaManager(Path stagingRoot) {
        this.stagingRoot = stagingRoot;
    }

    public StagingArea allocate(String deviceSerial, String packageName) throws IOException {
        String id = UUID.randomUUID().toString();
        Path dir = stagingRoot.resolve(id);
        Files.createDirectories(dir);
        StagingArea area = new StagingArea(id, deviceSerial, packageName, dir);
        activeStagingAreas.put(id, area);
        return area;
    }

    public StagingArea get(String stagingId) {
        return activeStagingAreas.get(stagingId);
    }

    public void release(String stagingId) throws IOException {
        StagingArea area = activeStagingAreas.remove(stagingId);
        if (area != null) {
            deleteRecursively(area.getDirectory());
        }
    }

    public boolean exists(String stagingId) {
        return activeStagingAreas.containsKey(stagingId);
    }

    public int activeCount() {
        return activeStagingAreas.size();
    }

    private void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (var stream = Files.walk(dir)) {
            stream.sorted(java.util.Comparator.reverseOrder())
                  .map(Path::toFile)
                  .forEach(java.io.File::delete);
        }
    }
}
