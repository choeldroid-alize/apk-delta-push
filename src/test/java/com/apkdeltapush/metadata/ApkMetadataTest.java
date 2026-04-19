package com.apkdeltapush.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApkMetadataTest {

    private ApkMetadata buildSample() {
        return new ApkMetadata(
                "com.example.app",
                42,
                "2.1.0",
                15_000_000L,
                "abc123def456",
                "21",
                "33"
        );
    }

    @Test
    void testFieldAccessors() {
        ApkMetadata meta = buildSample();
        assertEquals("com.example.app", meta.getPackageName());
        assertEquals(42, meta.getVersionCode());
        assertEquals("2.1.0", meta.getVersionName());
        assertEquals(15_000_000L, meta.getFileSizeBytes());
        assertEquals("abc123def456", meta.getSha256Checksum());
        assertEquals("21", meta.getMinSdkVersion());
        assertEquals("33", meta.getTargetSdkVersion());
    }

    @Test
    void testEqualityAndHashCode() {
        ApkMetadata a = buildSample();
        ApkMetadata b = buildSample();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testInequalityOnVersionCode() {
        ApkMetadata a = buildSample();
        ApkMetadata b = new ApkMetadata("com.example.app", 99, "3.0.0",
                15_000_000L, "abc123def456", "21", "33");
        assertNotEquals(a, b);
    }

    @Test
    void testInequalityOnChecksum() {
        ApkMetadata a = buildSample();
        ApkMetadata b = new ApkMetadata("com.example.app", 42, "2.1.0",
                15_000_000L, "differentchecksum", "21", "33");
        assertNotEquals(a, b);
    }

    @Test
    void testNullPackageNameThrows() {
        assertThrows(NullPointerException.class, () ->
                new ApkMetadata(null, 1, "1.0", 0L, "chk", "21", "33"));
    }

    @Test
    void testNullChecksumThrows() {
        assertThrows(NullPointerException.class, () ->
                new ApkMetadata("com.example.app", 1, "1.0", 0L, null, "21", "33"));
    }

    @Test
    void testToStringContainsPackageName() {
        assertTrue(buildSample().toString().contains("com.example.app"));
    }
}
