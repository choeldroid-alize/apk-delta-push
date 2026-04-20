package com.apkdeltapush.cleanup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages cleanup of temporary files and directories created during
 * delta push operations (staging areas, patch files, temp APKs, etc.).
 */
public class PushCleanupManager {

    private static final Logger logger = Logger.getLogger(PushCleanupManager.class.getName());

    private final List<Path> registeredPaths = new ArrayList<>();
    private boolean autoCleanupEnabled;

    public PushCleanupManager(boolean autoCleanupEnabled) {
        this.autoCleanupEnabled = autoCleanupEnabled;
    }

    /**
     * Registers a path for cleanup. Will be deleted when cleanup is triggered.
     */
    public void register(Path path) {
        if (path != null) {
            registeredPaths.add(path);
            logger.fine("Registered path for cleanup: " + path);
        }
    }

    /**
     * Performs cleanup of all registered paths.
     *
     * @return a CleanupResult summarizing what was deleted and any failures.
     */
    public CleanupResult cleanup() {
        int deleted = 0;
        List<Path> failed = new ArrayList<>();

        for (Path path : registeredPaths) {
            try {
                boolean removed = deleteRecursively(path);
                if (removed) {
                    deleted++;
                    logger.fine("Deleted: " + path);
                }
            } catch (IOException e) {
                logger.warning("Failed to delete: " + path + " — " + e.getMessage());
                failed.add(path);
            }
        }

        registeredPaths.clear();
        return new CleanupResult(deleted, failed);
    }

    /**
     * Clears all registered paths without deleting them.
     */
    public void reset() {
        registeredPaths.clear();
    }

    public boolean isAutoCleanupEnabled() {
        return autoCleanupEnabled;
    }

    public void setAutoCleanupEnabled(boolean autoCleanupEnabled) {
        this.autoCleanupEnabled = autoCleanupEnabled;
    }

    public List<Path> getRegisteredPaths() {
        return new ArrayList<>(registeredPaths);
    }

    private boolean deleteRecursively(Path path) throws IOException {
        File file = path.toFile();
        if (!file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child.toPath());
                }
            }
        }
        Files.delete(path);
        return true;
    }
}
