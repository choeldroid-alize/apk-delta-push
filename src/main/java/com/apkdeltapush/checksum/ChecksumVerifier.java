package com.apkdeltapush.checksum;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Computes and verifies SHA-256 checksums for APK and patch files.
 */
public class ChecksumVerifier {

    private static final String ALGORITHM = "SHA-256";

    /**
     * Computes the SHA-256 checksum of the given file.
     *
     * @param filePath path to the file
     * @return hex-encoded checksum string
     * @throws IOException if the file cannot be read
     */
    public String compute(Path filePath) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            try (InputStream is = Files.newInputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Verifies that the file's checksum matches the expected value.
     *
     * @param filePath path to the file
     * @param expected expected hex checksum
     * @return a ChecksumResult indicating match or mismatch
     */
    public ChecksumResult verify(Path filePath, String expected) throws IOException {
        String actual = compute(filePath);
        boolean match = actual.equalsIgnoreCase(expected);
        return new ChecksumResult(filePath, expected, actual, match);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
