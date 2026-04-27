package com.apkdeltapush.diff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.logging.Logger;

/**
 * Compresses and decompresses delta diff payloads using DEFLATE.
 * Sits between diff generation and transfer to reduce wire size.
 */
public class DeltaDiffCompressor {

    private static final Logger logger = Logger.getLogger(DeltaDiffCompressor.class.getName());

    private final int compressionLevel;

    public DeltaDiffCompressor() {
        this(Deflater.DEFAULT_COMPRESSION);
    }

    public DeltaDiffCompressor(int compressionLevel) {
        if (compressionLevel < Deflater.NO_COMPRESSION || compressionLevel > Deflater.BEST_COMPRESSION) {
            throw new IllegalArgumentException("Invalid compression level: " + compressionLevel);
        }
        this.compressionLevel = compressionLevel;
    }

    /**
     * Compresses raw delta diff bytes.
     *
     * @param input uncompressed delta bytes
     * @return compressed bytes
     * @throws IOException if compression fails
     */
    public byte[] compress(byte[] input) throws IOException {
        if (input == null || input.length == 0) {
            throw new IllegalArgumentException("Input must not be null or empty");
        }
        Deflater deflater = new Deflater(compressionLevel);
        deflater.setInput(input);
        deflater.finish();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length)) {
            byte[] buffer = new byte[4096];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                baos.write(buffer, 0, count);
            }
            byte[] result = baos.toByteArray();
            logger.fine(String.format("Compressed %d -> %d bytes (%.1f%%)",
                    input.length, result.length,
                    100.0 * result.length / input.length));
            return result;
        } finally {
            deflater.end();
        }
    }

    /**
     * Decompresses previously compressed delta diff bytes.
     *
     * @param input compressed delta bytes
     * @param expectedSize hint for output buffer sizing; use 0 if unknown
     * @return decompressed bytes
     * @throws IOException if decompression fails
     */
    public byte[] decompress(byte[] input, int expectedSize) throws IOException {
        if (input == null || input.length == 0) {
            throw new IllegalArgumentException("Input must not be null or empty");
        }
        Inflater inflater = new Inflater();
        inflater.setInput(input);
        int bufSize = expectedSize > 0 ? expectedSize : input.length * 4;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(bufSize)) {
            byte[] buffer = new byte[4096];
            while (!inflater.finished()) {
                int count;
                try {
                    count = inflater.inflate(buffer);
                } catch (java.util.zip.DataFormatException e) {
                    throw new IOException("Delta diff decompression failed", e);
                }
                baos.write(buffer, 0, count);
            }
            return baos.toByteArray();
        } finally {
            inflater.end();
        }
    }

    public int getCompressionLevel() {
        return compressionLevel;
    }
}
