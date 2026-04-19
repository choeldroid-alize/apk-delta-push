package com.apkdeltapush.manifest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class ApkManifestParserTest {

    private ApkManifestParser parser;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        parser = new ApkManifestParser();
    }

    private File createFakeApk(String name, boolean includeManifest) throws IOException {
        File apk = tempDir.resolve(name).toFile();
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(apk.toPath()))) {
            if (includeManifest) {
                zos.putNextEntry(new ZipEntry("AndroidManifest.xml"));
                zos.write(new byte[]{0x03, 0x00, 0x08, 0x00});
                zos.closeEntry();
            }
            zos.putNextEntry(new ZipEntry("classes.dex"));
            zos.write("dex content".getBytes());
            zos.closeEntry();
        }
        return apk;
    }

    @Test
    void parse_validApk_returnsManifestInfo() throws IOException {
        File apk = createFakeApk("com.example.app.apk", true);
        ManifestInfo info = parser.parse(apk.getAbsolutePath());

        assertNotNull(info);
        assertEquals(2, info.getEntryCount());
        assertTrue(info.getApkSize() > 0);
        assertTrue(info.getManifestSize() > 0);
        assertNotNull(info.getPackageName());
    }

    @Test
    void parse_missingManifest_throwsIOException() throws IOException {
        File apk = createFakeApk("noManifest.apk", false);
        assertThrows(IOException.class, () -> parser.parse(apk.getAbsolutePath()));
    }

    @Test
    void parse_nonExistentFile_throwsIOException() {
        assertThrows(IOException.class, () -> parser.parse("/nonexistent/path/fake.apk"));
    }

    @Test
    void parse_packageNameDerivedFromFilename() throws IOException {
        File apk = createFakeApk("my-app_release.apk", true);
        ManifestInfo info = parser.parse(apk.getAbsolutePath());
        assertNotNull(info.getPackageName());
        assertFalse(info.getPackageName().isEmpty());
    }
}
