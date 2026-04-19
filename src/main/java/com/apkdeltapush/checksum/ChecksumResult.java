package com.apkdeltapush.checksum;

import java.nio.file.Path;

/**
 * Holds the outcome of a checksum verification for a single file.
 */
public class ChecksumResult {

    private final Path filePath;
    private final String expected;
    private final String actual;
    private final boolean valid;

    public ChecksumResult(Path filePath, String expected, String actual, boolean valid) {
        this.filePath = filePath;
        this.expected = expected;
        this.actual = actual;
        this.valid = valid;
    }

    public Path getFilePath() { return filePath; }
    public String getExpected() { return expected; }
    public String getActual() { return actual; }
    public boolean isValid() { return valid; }

    @Override
    public String toString() {
        return String.format("ChecksumResult{file=%s, valid=%b, expected=%s, actual=%s}",
                filePath.getFileName(), valid, expected, actual);
    }
}
