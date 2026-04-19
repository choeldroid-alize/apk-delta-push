package com.apkdeltapush.checksum;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ChecksumResultTest {

    private static final Path FILE = Paths.get("/tmp/app.apk");
    private static final String EXPECTED = "abc123";
    private static final String ACTUAL = "abc123";

    @Test
    void isValidWhenChecksumMatches() {
        ChecksumResult result = new ChecksumResult(FILE, EXPECTED, ACTUAL, true);
        assertTrue(result.isValid());
    }

    @Test
    void isInvalidWhenChecksumMismatches() {
        ChecksumResult result = new ChecksumResult(FILE, EXPECTED, "fff000", false);
        assertFalse(result.isValid());
    }

    @Test
    void gettersReturnCorrectValues() {
        ChecksumResult result = new ChecksumResult(FILE, EXPECTED, ACTUAL, true);
        assertEquals(FILE, result.getFilePath());
        assertEquals(EXPECTED, result.getExpected());
        assertEquals(ACTUAL, result.getActual());
    }

    @Test
    void toStringContainsRelevantInfo() {
        ChecksumResult result = new ChecksumResult(FILE, EXPECTED, ACTUAL, true);
        String str = result.toString();
        assertTrue(str.contains("true"));
        assertTrue(str.contains("app.apk"));
    }
}
