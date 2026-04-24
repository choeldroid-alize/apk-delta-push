package com.apkdeltapush.dedup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeltaDeduplicatorTest {

    private DeltaDeduplicator deduplicator;

    private static final String SRC_HASH  = "aabbcc1122";
    private static final String TGT_HASH  = "ddeeff3344";
    private static final String DELTA_PATH = "/tmp/delta_aabbcc_ddeeff.patch";
    private static final long   DELTA_SIZE = 204800L;

    @BeforeEach
    void setUp() {
        deduplicator = new DeltaDeduplicator();
    }

    @Test
    void lookupReturnsEmptyWhenNoEntryRegistered() {
        Optional<DedupEntry> result = deduplicator.lookup(SRC_HASH, TGT_HASH);
        assertTrue(result.isEmpty(), "Expected empty Optional for unregistered pair");
    }

    @Test
    void registerAndLookupReturnsSameEntry() {
        deduplicator.register(SRC_HASH, TGT_HASH, DELTA_PATH, DELTA_SIZE);

        Optional<DedupEntry> result = deduplicator.lookup(SRC_HASH, TGT_HASH);
        assertTrue(result.isPresent());

        DedupEntry entry = result.get();
        assertEquals(SRC_HASH,   entry.getSourceHash());
        assertEquals(TGT_HASH,   entry.getTargetHash());
        assertEquals(DELTA_PATH, entry.getDeltaPath());
        assertEquals(DELTA_SIZE, entry.getDeltaSize());
        assertNotNull(entry.getCreatedAt());
    }

    @Test
    void lookupIsSensitiveToHashPairOrder() {
        deduplicator.register(SRC_HASH, TGT_HASH, DELTA_PATH, DELTA_SIZE);

        // reversed pair should NOT match
        Optional<DedupEntry> reversed = deduplicator.lookup(TGT_HASH, SRC_HASH);
        assertTrue(reversed.isEmpty(), "Reversed hash pair must not match");
    }

    @Test
    void evictRemovesEntry() {
        deduplicator.register(SRC_HASH, TGT_HASH, DELTA_PATH, DELTA_SIZE);
        assertTrue(deduplicator.evict(SRC_HASH, TGT_HASH));
        assertTrue(deduplicator.lookup(SRC_HASH, TGT_HASH).isEmpty());
    }

    @Test
    void evictReturnsFalseWhenEntryAbsent() {
        assertFalse(deduplicator.evict(SRC_HASH, TGT_HASH));
    }

    @Test
    void sizeTracksRegisteredEntries() {
        assertEquals(0, deduplicator.size());
        deduplicator.register(SRC_HASH, TGT_HASH, DELTA_PATH, DELTA_SIZE);
        assertEquals(1, deduplicator.size());
        deduplicator.register("xx", "yy", "/tmp/other.patch", 1024);
        assertEquals(2, deduplicator.size());
    }

    @Test
    void clearRemovesAllEntries() {
        deduplicator.register(SRC_HASH, TGT_HASH, DELTA_PATH, DELTA_SIZE);
        deduplicator.clear();
        assertEquals(0, deduplicator.size());
        assertTrue(deduplicator.lookup(SRC_HASH, TGT_HASH).isEmpty());
    }

    @Test
    void dedupEntryRejectsNegativeDeltaSize() {
        assertThrows(IllegalArgumentException.class,
            () -> new DedupEntry(SRC_HASH, TGT_HASH, DELTA_PATH, -1));
    }

    @Test
    void dedupEntryEqualityIgnoresCreatedAt() {
        DedupEntry a = new DedupEntry(SRC_HASH, TGT_HASH, DELTA_PATH, DELTA_SIZE);
        DedupEntry b = new DedupEntry(SRC_HASH, TGT_HASH, DELTA_PATH, DELTA_SIZE);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
