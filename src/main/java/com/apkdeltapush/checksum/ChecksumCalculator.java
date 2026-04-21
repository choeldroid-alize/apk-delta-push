package com.apkdeltapush.checksum;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for computing checksums (MD5, SHA-1, SHA-256) of APK files and delta patches.
 */
public class ChecksumCalculator {

    public enum Algorithm {
        MD5("MD5"),
        SHA1("SHA-1"),
        SHA256("SHA-256");

        private final String jcaName;

        Algorithm(String jcaName) {
            this.jcaName = jcaName;
        }

        public String getJcaName() {
            return jcaName;
        }
    }

    private static final int BUFFER_SIZE = 8192;

    /**
     * Computes the checksum of the file at the given path using the specified algorithm.
     *
     * @param filePath  path to the file
     * @param algorithm hashing algorithm to use
     * @return hex-encoded checksum string
     * @throws IOException              if the file cannot be read
     * @throws NoSuchAlgorithmException if the algorithm is unavailable
     */
    public String compute(Path filePath, Algorithm algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm.getJcaName());
        try (InputStream is = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        return bytesToHex(digest.digest());
    }

    /**
     * Computes the SHA-256 checksum of a byte array (e.g., an in-memory delta patch).
     *
     * @param data raw bytes
     * @return hex-encoded SHA-256 checksum
     * @throws NoSuchAlgorithmException if SHA-256 is unavailable
     */
    public String computeSha256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(Algorithm.SHA256.getJcaName());
        return bytesToHex(digest.digest(data));
    }

    /**
     * Verifies that the checksum of the given file matches the expected value.
     *
     * @param filePath         path to the file
     * @param algorithm        hashing algorithm
     * @param expectedChecksum expected hex-encoded checksum
     * @return true if the checksums match, false otherwise
     * @throws IOException              if the file cannot be read
     * @throws NoSuchAlgorithmException if the algorithm is unavailable
     */
    public boolean verify(Path filePath, Algorithm algorithm, String expectedChecksum)
            throws IOException, NoSuchAlgorithmException {
        String actual = compute(filePath, algorithm);
        return actual.equalsIgnoreCase(expectedChecksum);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
