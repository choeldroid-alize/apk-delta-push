package com.apkdeltapush.cache;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Manages a local cache of generated APK delta patches to avoid
 * regenerating diffs for the same source/target APK pair.
 */
public class DeltaCache {

    private static final Logger logger = Logger.getLogger(DeltaCache.class.getName());

    private final Path cacheDir;

    public DeltaCache(String cacheDirPath) throws IOException {
        this.cacheDir = Paths.get(cacheDirPath);
        if (!Files.exists(this.cacheDir)) {
            Files.createDirectories(this.cacheDir);
            logger.info("Created delta cache directory: " + cacheDirPath);
        }
    }

    /**
     * Returns the cached patch file for the given source and target APK checksums,
     * or null if no cached entry exists.
     */
    public File getCachedPatch(String sourceChecksum, String targetChecksum) {
        Path patchPath = resolvePatchPath(sourceChecksum, targetChecksum);
        if (Files.exists(patchPath)) {
            logger.fine("Cache hit for " + sourceChecksum + " -> " + targetChecksum);
            return patchPath.toFile();
        }
        logger.fine("Cache miss for " + sourceChecksum + " -> " + targetChecksum);
        return null;
    }

    /**
     * Stores a patch file in the cache under the key derived from source and target checksums.
     */
    public void storePatch(String sourceChecksum, String targetChecksum, File patchFile) throws IOException {
        if (patchFile == null || !patchFile.exists()) {
            throw new IllegalArgumentException("Patch file must exist before storing in cache.");
        }
        Path destination = resolvePatchPath(sourceChecksum, targetChecksum);
        Files.copy(patchFile.toPath(), destination,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        logger.info("Stored patch in cache: " + destination);
    }

    /**
     * Removes all entries from the cache directory.
     */
    public void clearCache() throws IOException {
        try (var stream = Files.list(cacheDir)) {
            stream.forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    logger.warning("Failed to delete cache entry: " + path);
                }
            });
        }
        logger.info("Delta cache cleared.");
    }

    public Path getCacheDir() {
        return cacheDir;
    }

    private Path resolvePatchPath(String sourceChecksum, String targetChecksum) {
        String filename = sourceChecksum + "_" + targetChecksum + ".patch";
        return cacheDir.resolve(filename);
    }
}
