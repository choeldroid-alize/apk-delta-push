package com.apkdeltapush.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DeltaDiffCacheTest {

    private DeltaDiffCache cache;

    @BeforeEach
    void setUp() {
        cache = new DeltaDiffCache(4);
    }

    @Test
    void putAndGet_returnsStoredResult() {
        DeltaDiffResult result = mock(DeltaDiffResult.class);
        cache.put("src1", "tgt1", result);

        Optional<DeltaDiffResult> retrieved = cache.get("src1", "tgt1");
        assertTrue(retrieved.isPresent());
        assertSame(result, retrieved.get());
    }

    @Test
    void get_missingKey_returnsEmpty() {
        Optional<DeltaDiffResult> result = cache.get("missing", "key");
        assertFalse(result.isPresent());
    }

    @Test
    void contains_returnsTrueAfterPut() {
        cache.put("a", "b", mock(DeltaDiffResult.class));
        assertTrue(cache.contains("a", "b"));
        assertFalse(cache.contains("a", "c"));
    }

    @Test
    void invalidate_removesEntry() {
        cache.put("x", "y", mock(DeltaDiffResult.class));
        cache.invalidate("x", "y");
        assertFalse(cache.contains("x", "y"));
        assertEquals(0, cache.size());
    }

    @Test
    void clear_removesAllEntries() {
        cache.put("a", "b", mock(DeltaDiffResult.class));
        cache.put("c", "d", mock(DeltaDiffResult.class));
        cache.clear();
        assertEquals(0, cache.size());
    }

    @Test
    void lruEviction_evictsEldestWhenFull() {
        DeltaDiffResult r1 = mock(DeltaDiffResult.class);
        DeltaDiffResult r2 = mock(DeltaDiffResult.class);
        DeltaDiffResult r3 = mock(DeltaDiffResult.class);
        DeltaDiffResult r4 = mock(DeltaDiffResult.class);
        DeltaDiffResult r5 = mock(DeltaDiffResult.class);

        cache.put("s1", "t1", r1);
        cache.put("s2", "t2", r2);
        cache.put("s3", "t3", r3);
        cache.put("s4", "t4", r4);
        // Access s1 to make it recently used
        cache.get("s1", "t1");
        // Adding a 5th entry should evict the least recently used (s2)
        cache.put("s5", "t5", r5);

        assertEquals(4, cache.size());
        assertTrue(cache.contains("s1", "t1"));
        assertFalse(cache.contains("s2", "t2"));
        assertTrue(cache.contains("s5", "t5"));
    }

    @Test
    void buildKey_nullChecksum_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> cache.buildKey(null, "tgt"));
        assertThrows(IllegalArgumentException.class, () -> cache.buildKey("src", null));
    }

    @Test
    void constructor_invalidMaxEntries_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new DeltaDiffCache(0));
        assertThrows(IllegalArgumentException.class, () -> new DeltaDiffCache(-5));
    }

    @Test
    void getMaxEntries_returnsConfiguredValue() {
        DeltaDiffCache c = new DeltaDiffCache(16);
        assertEquals(16, c.getMaxEntries());
    }
}
