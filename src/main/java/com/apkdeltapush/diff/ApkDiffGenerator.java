package com.apkdeltapush.diff;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 * Generates a binary delta patch between two APK files using bsdiff-style
 * approach. For simplicity in this implementation, we produce a raw binary
 * diff payload along with metadata needed to apply it on-device.
 */
public class ApkDiffGenerator {

    private static final Logger LOG = Logger.getLogger(ApkDiffGenerator.class.getName());

    /**
     * Generates a DiffResult containing the patch bytes and checksums.
     *
     * @param oldApk path to the currently installed APK (pulled from device)
     * @param newApk path to the new APK to be pushed
     * @return DiffResult with patch data
     * @throws IOException if file reading fails
     */
    public DiffResult generate(Path oldApk, Path newApk) throws IOException {
        LOG.info("Generating diff: " + oldApk + " -> " + newApk);

        byte[] oldBytes = Files.readAllBytes(oldApk);
        byte[] newBytes = Files.readAllBytes(newApk);

        byte[] patch = computePatch(oldBytes, newBytes);

        String oldChecksum = sha256(oldBytes);
        String newChecksum = sha256(newBytes);

        long savedBytes = newBytes.length - patch.length;
        double ratio = (patch.length * 100.0) / newBytes.length;

        LOG.info(String.format("Patch size: %d bytes (%.1f%% of original, saved %d bytes)",
                patch.length, ratio, savedBytes));

        return new DiffResult(patch, oldChecksum, newChecksum, oldBytes.length, newBytes.length);
    }

    /**
     * Naive XOR-based patch for demonstration. Replace with bsdiff/xdelta in production.
     */
    private byte[] computePatch(byte[] oldBytes, byte[] newBytes) {
        int minLen = Math.min(oldBytes.length, newBytes.length);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Header: 4-byte new length
        out.write((newBytes.length >> 24) & 0xFF);
        out.write((newBytes.length >> 16) & 0xFF);
        out.write((newBytes.length >> 8) & 0xFF);
        out.write(newBytes.length & 0xFF);

        for (int i = 0; i < minLen; i++) {
            out.write(newBytes[i] ^ oldBytes[i]);
        }
        // Append extra bytes if new APK is larger
        for (int i = minLen; i < newBytes.length; i++) {
            out.write(newBytes[i]);
        }
        return out.toByteArray();
    }

    private String sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
