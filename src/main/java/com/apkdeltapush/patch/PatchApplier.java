package com.apkdeltapush.patch;

import java.io.*;
import java.nio.file.*;
import java.util.logging.Logger;

/**
 * Applies a binary delta patch to a base APK to produce an updated APK.
 */
public class PatchApplier {

    private static final Logger logger = Logger.getLogger(PatchApplier.class.getName());

    /**
     * Applies the given patch file to the base APK and writes the result to outputApk.
     *
     * @param baseApk   path to the original APK on disk
     * @param patchFile path to the binary delta patch
     * @param outputApk path where the patched APK should be written
     * @throws IOException if any I/O error occurs
     */
    public void apply(Path baseApk, Path patchFile, Path outputApk) throws IOException {
        if (!Files.exists(baseApk)) {
            throw new FileNotFoundException("Base APK not found: " + baseApk);
        }
        if (!Files.exists(patchFile)) {
            throw new FileNotFoundException("Patch file not found: " + patchFile);
        }

        logger.info("Applying patch " + patchFile + " to " + baseApk);

        byte[] baseBytes = Files.readAllBytes(baseApk);
        byte[] patchBytes = Files.readAllBytes(patchFile);
        byte[] outputBytes = applyDelta(baseBytes, patchBytes);

        Files.write(outputApk, outputBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        logger.info("Patched APK written to " + outputApk + " (" + outputBytes.length + " bytes)");
    }

    /**
     * Simple XOR-based delta application (placeholder for a real bsdiff/xdelta implementation).
     */
    byte[] applyDelta(byte[] base, byte[] patch) {
        if (patch.length < 4) {
            throw new IllegalArgumentException("Patch data too short to contain header");
        }
        // Header: first 4 bytes encode expected output length
        int outputLen = ((patch[0] & 0xFF) << 24) | ((patch[1] & 0xFF) << 16)
                | ((patch[2] & 0xFF) << 8) | (patch[3] & 0xFF);
        byte[] output = new byte[outputLen];
        for (int i = 0; i < outputLen; i++) {
            byte baseByte = (i < base.length) ? base[i] : 0;
            byte patchByte = (i + 4 < patch.length) ? patch[i + 4] : 0;
            output[i] = (byte) (baseByte ^ patchByte);
        }
        return output;
    }
}
