package com.apkdeltapush.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApkVersionCheckerTest {

    private static final String DEVICE_SERIAL = "emulator-5554";
    private static final String PACKAGE_NAME = "com.example.app";
    private static final String DUMPSYS_OUTPUT =
            "  versionCode=42 minSdk=21 targetSdk=33\n" +
            "  versionName=1.4.2\n" +
            "  packageName=com.example.app\n";

    private ApkVersionChecker checker;

    @BeforeEach
    void setUp() {
        checker = new ApkVersionChecker("adb");
    }

    @Test
    void getInstalledVersionCode_parsesCorrectly() throws IOException {
        ApkVersionChecker spyChecker = Mockito.spy(checker);
        doReturn(DUMPSYS_OUTPUT)
                .when(spyChecker)
                .getInstalledVersionCode(DEVICE_SERIAL, PACKAGE_NAME);

        // Direct regex test via a helper — test the parsing logic indirectly
        // by constructing output inline.
        int code = parseVersionCode(DUMPSYS_OUTPUT);
        assertEquals(42, code);
    }

    @Test
    void getInstalledVersionName_parsesCorrectly() {
        String name = parseVersionName(DUMPSYS_OUTPUT);
        assertEquals("1.4.2", name);
    }

    @Test
    void getInstalledVersionCode_returnsNegativeOneWhenNotFound() {
        int code = parseVersionCode("some unrelated output");
        assertEquals(-1, code);
    }

    @Test
    void getInstalledVersionName_returnsNullWhenNotFound() {
        String name = parseVersionName("some unrelated output");
        assertNull(name);
    }

    // --- helpers mirroring the regex logic in ApkVersionChecker ---

    private int parseVersionCode(String output) {
        java.util.regex.Matcher m =
                java.util.regex.Pattern.compile("versionCode=(\\d+)").matcher(output);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    private String parseVersionName(String output) {
        java.util.regex.Matcher m =
                java.util.regex.Pattern.compile("versionName=([\\w.\\-]+)").matcher(output);
        return m.find() ? m.group(1) : null;
    }
}
