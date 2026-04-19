package com.apkdeltapush.compress;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Handles gzip compression and decompression of delta patch files
 * to reduce transfer size over ADB.
 */
public class DeltaCompressor {

    private static final int BUFFER_SIZE = 8192;

    /**
     * Compresses the given delta file and writes output to destPath.
     *
     * @param sourcePath path to the uncompressed delta patch
     * @param destPath   path where the compressed output will be written
     * @return CompressionResult with size stats
     * @throws IOException if compression fails
     */
    public CompressionResult compress(Path sourcePath, Path destPath) throws IOException {
        long originalSize = Files.size(sourcePath);

        try (InputStream in = new BufferedInputStream(Files.newInputStream(sourcePath), BUFFER_SIZE);
             GZIPOutputStream gzOut = new GZIPOutputStream(
                     new BufferedOutputStream(Files.newOutputStream(destPath), BUFFER_SIZE))) {
            byte[] buf = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buf)) != -1) {
                gzOut.write(buf, 0, read);
            }
        }

        long compressedSize = Files.size(destPath);
        return new CompressionResult(originalSize, compressedSize, destPath);
    }

    /**
     * Decompresses a gzip-compressed delta file.
     *
     * @param sourcePath path to the compressed delta patch
     * @param destPath   path where the decompressed output will be written
     * @throws IOException if decompression fails
     */
    public void decompress(Path sourcePath, Path destPath) throws IOException {
        try (GZIPInputStream gzIn = new GZIPInputStream(
                new BufferedInputStream(Files.newInputStream(sourcePath), BUFFER_SIZE));
             OutputStream out = new BufferedOutputStream(Files.newOutputStream(destPath), BUFFER_SIZE)) {
            byte[] buf = new byte[BUFFER_SIZE];
            int read;
            while ((read = gzIn.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
        }
    }

    /**
     * Returns whether a file appears to be gzip-compressed by checking magic bytes.
     */
    public boolean isCompressed(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            byte[] magic = new byte[2];
            int bytesRead = in.read(magic);
            return bytesRead == 2 && magic[0] == (byte) 0x1f && magic[1] == (byte) 0x8b;
        }
    }
}
