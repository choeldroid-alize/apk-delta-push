package com.apkdeltapush.diff;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Orchestrates delta diff generation by selecting and invoking the appropriate
 * diff engine based on the configured {@link DeltaDiffStrategy}.
 */
public class DeltaDiffEngine {

    private static final Logger LOGGER = Logger.getLogger(DeltaDiffEngine.class.getName());

    private final DeltaDiffOptions options;
    private final BsdiffDeltaEngine bsdiffEngine;

    public DeltaDiffEngine(DeltaDiffOptions options) {
        this.options = Objects.requireNonNull(options, "options must not be null");
        this.bsdiffEngine = new BsdiffDeltaEngine();
    }

    /**
     * Generates a delta patch between {@code oldApk} and {@code newApk}.
     *
     * @param oldApk   path to the old (base) APK
     * @param newApk   path to the new (target) APK
     * @param outputDir directory where the delta file will be written
     * @return a {@link DeltaDiffResult} describing the outcome
     */
    public DeltaDiffResult generate(Path oldApk, Path newApk, Path outputDir) {
        Objects.requireNonNull(oldApk, "oldApk must not be null");
        Objects.requireNonNull(newApk, "newApk must not be null");
        Objects.requireNonNull(outputDir, "outputDir must not be null");

        try {
            validateInputs(oldApk, newApk, outputDir);

            long originalSize = Files.size(oldApk);
            long patchedSize = Files.size(newApk);

            DeltaDiffStrategy strategy = options.getStrategy();
            LOGGER.info(String.format("Generating delta using strategy=%s for %s -> %s",
                    strategy, oldApk.getFileName(), newApk.getFileName()));

            Path deltaFile = outputDir.resolve(buildDeltaFileName(oldApk, newApk));

            switch (strategy) {
                case BSDIFF:
                    bsdiffEngine.diff(oldApk, newApk, deltaFile, options);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported strategy: " + strategy);
            }

            long deltaSize = Files.size(deltaFile);
            LOGGER.info(String.format("Delta generated: %s (%.1f%% of original)",
                    deltaFile.getFileName(), (double) deltaSize / originalSize * 100));

            return DeltaDiffResult.builder()
                    .deltaFile(deltaFile)
                    .originalSize(originalSize)
                    .patchedSize(patchedSize)
                    .deltaSize(deltaSize)
                    .strategy(strategy)
                    .success(true)
                    .build();

        } catch (Exception e) {
            LOGGER.warning("Delta generation failed: " + e.getMessage());
            return DeltaDiffResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .strategy(options.getStrategy())
                    .build();
        }
    }

    private void validateInputs(Path oldApk, Path newApk, Path outputDir) throws IOException {
        if (!Files.isRegularFile(oldApk)) {
            throw new IllegalArgumentException("oldApk does not exist or is not a file: " + oldApk);
        }
        if (!Files.isRegularFile(newApk)) {
            throw new IllegalArgumentException("newApk does not exist or is not a file: " + newApk);
        }
        if (!Files.isDirectory(outputDir)) {
            Files.createDirectories(outputDir);
        }
    }

    private String buildDeltaFileName(Path oldApk, Path newApk) {
        String oldName = stripExtension(oldApk.getFileName().toString());
        String newName = stripExtension(newApk.getFileName().toString());
        return oldName + "_to_" + newName + ".delta";
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }
}
