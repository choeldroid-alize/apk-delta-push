package com.apkdeltapush.compress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DeltaCompressorTest {

    private DeltaCompressor compressor;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        compressor = new DeltaCompressor();
    }

    @Test
    void compress_producesCompressedFile() throws IOException {
        Path source = tempDir.resolve("delta.patch");
        Path dest = tempDir.resolve("delta.patch.gz");
        Files.writeString(source, "a".repeat(1000), StandardCharsets.UTF_8);

        CompressionResult result = compressor.compress(source, dest);

        assertTrue(Files.exists(dest));
        assertTrue(result.getCompressedSize() < result.getOriginalSize(),
                "Compressed size should be smaller than original for repetitive data");
        assertEquals(1000, result.getOriginalSize());
    }

    @Test
    void decompress_restoresOriginalContent() throws IOException {
        Path source = tempDir.resolve("delta.patch");
        Path compressed = tempDir.resolve("delta.patch.gz");
        Path restored = tempDir.resolve("delta.patch.restored");
        String content = "binary-delta-data-sample-12345";
        Files.writeString(source, content, StandardCharsets.UTF_8);

        compressor.compress(source, compressed);
        compressor.decompress(compressed, restored);

        String restoredContent = Files.readString(restored, StandardCharsets.UTF_8);
        assertEquals(content, restoredContent);
    }

    @Test
    void isCompressed_returnsTrueForGzipFile() throws IOException {
        Path source = tempDir.resolve("input.bin");
        Path compressed = tempDir.resolve("input.bin.gz");
        Files.writeString(source, "test data", StandardCharsets.UTF_8);
        compressor.compress(source, compressed);

        assertTrue(compressor.isCompressed(compressed));
    }

    @Test
    void isCompressed_returnsFalseForPlainFile() throws IOException {
        Path plain = tempDir.resolve("plain.patch");
        Files.writeString(plain, "not compressed", StandardCharsets.UTF_8);

        assertFalse(compressor.isCompressed(plain));
    }

    @Test
    void compressionResult_savingsRatioIsAccurate() throws IOException {
        Path source = tempDir.resolve("large.patch");
        Path dest = tempDir.resolve("large.patch.gz");
        Files.writeString(source, "x".repeat(5000), StandardCharsets.UTF_8);

        CompressionResult result = compressor.compress(source, dest);

        assertTrue(result.getSavingsRatio() > 0.5, "Expected >50% savings for highly repetitive input");
    }
}
