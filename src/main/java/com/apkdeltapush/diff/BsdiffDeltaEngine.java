package com.apkdeltapush.diff;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * BsdiffDeltaEngine implements delta generation using the bsdiff algorithm,
 * which produces highly compressed binary diffs well-suited for APK files.
 *
 * <p>This engine is selected when {@link DeltaDiffStrategy} is set to BSDIFF.
 * It shells out to the native {@code bsdiff} binary if available, or falls back
 * to a pure-Java implementation for portability.
 */
public class BsdiffDeltaEngine {

    private static final Logger logger = Logger.getLogger(BsdiffDeltaEngine.class.getName());

    /** Minimum size (bytes) below which bsdiff overhead is not worth it. */
    private static final int MIN_PATCH_SIZE_BYTES = 1024;

    private final DeltaDiffOptions options;
    private final boolean nativeBsdiffAvailable;

    public BsdiffDeltaEngine(DeltaDiffOptions options) {
        this.options = options;
        this.nativeBsdiffAvailable = checkNativeBsdiff();
        if (nativeBsdiffAvailable) {
            logger.fine("Native bsdiff binary found; will use native mode.");
        } else {
            logger.fine("Native bsdiff not found; falling back to pure-Java bsdiff.");
        }
    }

    /**
     * Generates a binary delta patch from {@code oldApk} to {@code newApk}.
     *
     * @param oldApk path to the old (installed) APK
     * @param newApk path to the new APK
     * @param patchOutput destination path for the generated patch file
     * @throws IOException if reading/writing files fails or the diff process errors
     */
    public void generatePatch(Path oldApk, Path newApk, Path patchOutput) throws IOException {
        long oldSize = Files.size(oldApk);
        long newSize = Files.size(newApk);
        logger.info(String.format("Generating bsdiff patch: old=%d bytes, new=%d bytes", oldSize, newSize));

        if (newSize < MIN_PATCH_SIZE_BYTES) {
            logger.warning("New APK is very small; copying verbatim instead of diffing.");
            Files.copy(newApk, patchOutput);
            return;
        }

        if (nativeBsdiffAvailable && options.isPreferNativeDiff()) {
            generatePatchNative(oldApk, newApk, patchOutput);
        } else {
            generatePatchJava(oldApk, newApk, patchOutput);
        }

        long patchSize = Files.size(patchOutput);
        double ratio = (double) patchSize / newSize * 100.0;
        logger.info(String.format("Patch generated: %d bytes (%.1f%% of new APK size)", patchSize, ratio));
    }

    /**
     * Invokes the native {@code bsdiff} binary via a subprocess.
     */
    private void generatePatchNative(Path oldApk, Path newApk, Path patchOutput) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "bsdiff",
                oldApk.toAbsolutePath().toString(),
                newApk.toAbsolutePath().toString(),
                patchOutput.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Drain stdout/stderr to avoid blocking
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.fine("[bsdiff] " + line);
            }
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("bsdiff process interrupted", e);
        }

        if (exitCode != 0) {
            throw new IOException("Native bsdiff exited with code " + exitCode);
        }
    }

    /**
     * Pure-Java fallback: reads both APKs into memory and writes a simple
     * XOR-based delta. This is intentionally lightweight; for production use
     * the native binary is strongly preferred.
     */
    private void generatePatchJava(Path oldApk, Path newApk, Path patchOutput) throws IOException {
        byte[] oldBytes = Files.readAllBytes(oldApk);
        byte[] newBytes = Files.readAllBytes(newApk);
        int patchLen = newBytes.length;
        byte[] patch = Arrays.copyOf(newBytes, patchLen);

        // XOR matching region so patch carries only the difference
        int common = Math.min(oldBytes.length, newBytes.length);
        for (int i = 0; i < common; i++) {
            patch[i] = (byte) (newBytes[i] ^ oldBytes[i]);
        }

        // Write 8-byte header: original size (4 bytes) + new size (4 bytes)
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(patchOutput)))) {
            out.writeInt(oldBytes.length);
            out.writeInt(newBytes.length);
            out.write(patch);
        }
    }

    /**
     * Checks whether the {@code bsdiff} binary is accessible on PATH.
     */
    private boolean checkNativeBsdiff() {
        try {
            Process p = new ProcessBuilder("bsdiff").start();
            p.destroy();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Returns true if the native bsdiff binary was detected at construction time. */
    public boolean isNativeBsdiffAvailable() {
        return nativeBsdiffAvailable;
    }
}
