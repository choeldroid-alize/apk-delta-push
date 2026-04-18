package com.apkdeltapush.patch;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.HexFormat;
import java.util.logging.Logger;

/**
 * Validates the integrity of a patch file using SHA-256 checksums.
 */
public class PatchValidator {

    private static final Logger logger = Logger.getLogger(PatchValidator.class.getName());

    /**
     * Computes the SHA-256 hex digest of the given file.
     */
    public String computeChecksum(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    /**
     * Validates that the patch file's checksum matches the expected value.
     *
     * @param patchFile        path to the patch file
     * @param expectedChecksum expected SHA-256 hex string
     * @return true if valid, false otherwise
     */
    public boolean validate(Path patchFile, String expectedChecksum) {
        if (!Files.exists(patchFile)) {
            logger.warning("Patch file does not exist: " + patchFile);
            return false;
        }
        try {
            String actual = computeChecksum(patchFile);
            boolean match = actual.equalsIgnoreCase(expectedChecksum);
            if (!match) {
                logger.warning("Checksum mismatch. Expected: " + expectedChecksum + ", got: " + actual);
            }
            return match;
        } catch (IOException | NoSuchAlgorithmException e) {
            logger.severe("Validation failed: " + e.getMessage());
            return false;
        }
    }
}
