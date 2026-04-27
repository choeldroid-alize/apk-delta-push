package com.apkdeltapush.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;

import static org.junit.jupiter.api.Assertions.*;

class DeltaDiffCompressorTest {

    private DeltaDiffCompressor compressor;

    @BeforeEach
    void setUp() {
        compressor = new DeltaDiffCompressor();
    }

    @Test
    void defaultCompressionLevelIsDefaultCompression() {
        assertEquals(Deflater.DEFAULT_COMPRESSION, compressor.getCompressionLevel());
    }

    @Test
    void customCompressionLevelIsStored() {
        DeltaDiffCompressor c = new DeltaDiffCompressor(Deflater.BEST_SPEED);
        assertEquals(Deflater.BEST_SPEED, c.getCompressionLevel());
    }

    @Test
    void invalidCompressionLevelThrows() {
        assertThrows(IllegalArgumentException.class, () -> new DeltaDiffCompressor(42));
        assertThrows(IllegalArgumentException.class, () -> new DeltaDiffCompressor(-5));
    }

    @Test
    void compressAndDecompressRoundTrip() throws IOException {
        byte[] original = "Hello, delta diff compression round-trip test!"
                .getBytes(StandardCharsets.UTF_8);
        byte[] compressed = compressor.compress(original);
        byte[] restored = compressor.decompress(compressed, original.length);
        assertArrayEquals(original, restored);
    }

    @Test
    void compressedSizeIsSmallerForRepetitiveData() throws IOException {
        // Highly repetitive payload compresses well
        byte[] repetitive = new byte[2048];
        for (int i = 0; i < repetitive.length; i++) {
            repetitive[i] = (byte) (i % 8);
        }
        byte[] compressed = compressor.compress(repetitive);
        assertTrue(compressed.length < repetitive.length,
                "Expected compressed size < original for repetitive data");
    }

    @Test
    void compressNullInputThrows() {
        assertThrows(IllegalArgumentException.class, () -> compressor.compress(null));
    }

    @Test
    void compressEmptyInputThrows() {
        assertThrows(IllegalArgumentException.class, () -> compressor.compress(new byte[0]));
    }

    @Test
    void decompressNullInputThrows() {
        assertThrows(IllegalArgumentException.class, () -> compressor.decompress(null, 0));
    }

    @Test
    void decompressEmptyInputThrows() {
        assertThrows(IllegalArgumentException.class, () -> compressor.decompress(new byte[0], 0));
    }

    @Test
    void decompressCorruptDataThrowsIOException() {
        byte[] garbage = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
        assertThrows(IOException.class, () -> compressor.decompress(garbage, 64));
    }

    @Test
    void decompressWithZeroExpectedSizeStillWorks() throws IOException {
        byte[] original = "zero hint size test".getBytes(StandardCharsets.UTF_8);
        byte[] compressed = compressor.compress(original);
        byte[] restored = compressor.decompress(compressed, 0);
        assertArrayEquals(original, restored);
    }
}
