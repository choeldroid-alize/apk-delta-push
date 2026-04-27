package com.apkdeltapush.diff;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Applies a computed delta diff to a base APK to produce a patched output APK.
 * Works in conjunction with DeltaDiffEngine and DeltaDiffResult.
 */
public class DeltaDiffPatcher {

    private static final Logger logger = Logger.getLogger(DeltaDiffPatcher.class.getName());

    private final DeltaDiffPatchOptions options;

    public DeltaDiffPatcher(DeltaDiffPatchOptions options) {
        this.options = Objects.requireNonNull(options, "options must not be null");
    }

    /**
     * Applies the given diff result to the base file, writing the output to targetPath.
     *
     * @param baseApk    path to the original APK
     * @param diffResult the computed delta diff to apply
     * @param targetPath destination path for the patched APK
     * @throws IOException if reading/writing fails
     * @throws DeltaDiffPatchException if the patch cannot be applied cleanly
     */
    public DeltaDiffPatchResult patch(Path baseApk, DeltaDiffResult diffResult, Path targetPath)
            throws IOException, DeltaDiffPatchException {
        Objects.requireNonNull(baseApk, "baseApk must not be null");
        Objects.requireNonNull(diffResult, "diffResult must not be null");
        Objects.requireNonNull(targetPath, "targetPath must not be null");

        if (!Files.exists(baseApk)) {
            throw new DeltaDiffPatchException("Base APK not found: " + baseApk);
        }

        if (diffResult.getDeltaBytes() == null || diffResult.getDeltaBytes().length == 0) {
            throw new DeltaDiffPatchException("DeltaDiffResult contains no delta bytes");
        }

        logger.info("Applying delta patch to: " + baseApk + " -> " + targetPath);

        long startMs = System.currentTimeMillis();
        byte[] baseBytes = Files.readAllBytes(baseApk);
        byte[] patchedBytes = applyDelta(baseBytes, diffResult.getDeltaBytes());

        if (options.isValidateOutputSize()) {
            validateOutputSize(patchedBytes, diffResult);
        }

        Files.createDirectories(targetPath.getParent());
        Files.write(targetPath, patchedBytes);

        long elapsedMs = System.currentTimeMillis() - startMs;
        logger.info(String.format("Patch applied in %d ms, output size: %d bytes", elapsedMs, patchedBytes.length));

        return new DeltaDiffPatchResult(targetPath, patchedBytes.length, elapsedMs, true);
    }

    private byte[] applyDelta(byte[] base, byte[] delta) throws DeltaDiffPatchException {
        // Simplified XOR-based demonstration; real impl would use bsdiff/bspatch
        if (delta.length < base.length) {
            throw new DeltaDiffPatchException("Delta is shorter than base; cannot apply patch");
        }
        byte[] output = new byte[delta.length];
        for (int i = 0; i < base.length; i++) {
            output[i] = (byte) (base[i] ^ delta[i]);
        }
        System.arraycopy(delta, base.length, output, base.length, delta.length - base.length);
        return output;
    }

    private void validateOutputSize(byte[] patchedBytes, DeltaDiffResult diffResult)
            throws DeltaDiffPatchException {
        long expected = diffResult.getTargetSize();
        if (expected > 0 && patchedBytes.length != expected) {
            throw new DeltaDiffPatchException(
                String.format("Output size mismatch: expected %d bytes, got %d bytes", expected, patchedBytes.length));
        }
    }
}
