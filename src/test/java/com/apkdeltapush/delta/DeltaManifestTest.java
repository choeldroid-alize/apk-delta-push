package com.apkdeltapush.delta;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeltaManifestTest {

    private DeltaManifest buildSample() {
        return DeltaManifest.builder()
                .packageName("com.example.app")
                .sourceVersion("1.0.0")
                .targetVersion("1.1.0")
                .deltaSize(204_800L)
                .deltaChecksum("abc123def456")
                .createdAt(1_700_000_000_000L)
                .addChunkOffset("chunk-0", 0L)
                .addChunkOffset("chunk-1", 65_536L)
                .build();
    }

    @Test
    void shouldStoreAllFields() {
        DeltaManifest m = buildSample();
        assertEquals("com.example.app", m.getPackageName());
        assertEquals("1.0.0",           m.getSourceVersion());
        assertEquals("1.1.0",           m.getTargetVersion());
        assertEquals(204_800L,          m.getDeltaSize());
        assertEquals("abc123def456",    m.getDeltaChecksum());
        assertEquals(1_700_000_000_000L, m.getCreatedAt());
    }

    @Test
    void shouldStoreChunkOffsets() {
        DeltaManifest m = buildSample();
        assertEquals(2, m.getChunkOffsets().size());
        assertEquals(0L,      m.getChunkOffsets().get("chunk-0"));
        assertEquals(65_536L, m.getChunkOffsets().get("chunk-1"));
    }

    @Test
    void chunkOffsetsShouldBeImmutable() {
        DeltaManifest m = buildSample();
        assertThrows(UnsupportedOperationException.class,
                () -> m.getChunkOffsets().put("chunk-2", 131_072L));
    }

    @Test
    void isUpgradeShouldReturnTrueWhenVersionsDiffer() {
        assertTrue(buildSample().isUpgrade());
    }

    @Test
    void isUpgradeShouldReturnFalseWhenVersionsMatch() {
        DeltaManifest m = DeltaManifest.builder()
                .packageName("com.example.app")
                .sourceVersion("2.0.0")
                .targetVersion("2.0.0")
                .deltaSize(0L)
                .deltaChecksum("0000")
                .build();
        assertFalse(m.isUpgrade());
    }

    @Test
    void shouldThrowWhenRequiredFieldMissing() {
        assertThrows(NullPointerException.class, () ->
                DeltaManifest.builder()
                        .targetVersion("1.0")
                        .packageName("com.x")
                        .deltaSize(0)
                        .deltaChecksum("x")
                        .build());
    }

    @Test
    void toStringShouldContainKeyInfo() {
        String s = buildSample().toString();
        assertTrue(s.contains("com.example.app"));
        assertTrue(s.contains("1.0.0"));
        assertTrue(s.contains("1.1.0"));
    }
}
