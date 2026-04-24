package com.apkdeltapush.fragmentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DeltaFragmentManagerTest {

    private static final int FRAGMENT_SIZE = 1024; // 1 KB for tests
    private DeltaFragmentManager manager;

    @BeforeEach
    void setUp() {
        manager = new DeltaFragmentManager(FRAGMENT_SIZE);
    }

    @Test
    void fragment_splitsBytesIntoCorrectNumberOfChunks() {
        byte[] data = new byte[3 * FRAGMENT_SIZE + 100];
        new Random(42).nextBytes(data);

        List<DeltaFragment> fragments = manager.fragment("session-1", data);

        assertEquals(4, fragments.size());
        assertEquals(FRAGMENT_SIZE, fragments.get(0).getData().length);
        assertEquals(FRAGMENT_SIZE, fragments.get(1).getData().length);
        assertEquals(FRAGMENT_SIZE, fragments.get(2).getData().length);
        assertEquals(100, fragments.get(3).getData().length);
    }

    @Test
    void fragment_singleChunkWhenDataFitsInOneFragment() {
        byte[] data = new byte[500];
        List<DeltaFragment> fragments = manager.fragment("session-2", data);

        assertEquals(1, fragments.size());
        assertEquals(0, fragments.get(0).getIndex());
        assertEquals(1, fragments.get(0).getTotalFragments());
    }

    @Test
    void fragment_emptyDataProducesSingleEmptyFragment() {
        List<DeltaFragment> fragments = manager.fragment("session-3", new byte[0]);
        assertEquals(1, fragments.size());
        assertEquals(0, fragments.get(0).getData().length);
    }

    @Test
    void reassemble_reconstructsOriginalData() {
        byte[] original = new byte[3 * FRAGMENT_SIZE + 77];
        new Random(7).nextBytes(original);

        List<DeltaFragment> fragments = manager.fragment("session-4", original);
        byte[] reassembled = manager.reassemble(fragments);

        assertArrayEquals(original, reassembled);
    }

    @Test
    void reassemble_toleratesOutOfOrderFragments() {
        byte[] original = new byte[2 * FRAGMENT_SIZE];
        new Random(99).nextBytes(original);

        List<DeltaFragment> fragments = new ArrayList<>(manager.fragment("session-5", original));
        // Reverse order
        java.util.Collections.reverse(fragments);

        byte[] reassembled = manager.reassemble(fragments);
        assertArrayEquals(original, reassembled);
    }

    @Test
    void reassemble_throwsOnMissingFragment() {
        byte[] data = new byte[3 * FRAGMENT_SIZE];
        List<DeltaFragment> fragments = new ArrayList<>(manager.fragment("session-6", data));
        fragments.remove(1); // remove middle fragment

        assertThrows(IllegalArgumentException.class, () -> manager.reassemble(fragments));
    }

    @Test
    void constructor_throwsOnNonPositiveFragmentSize() {
        assertThrows(IllegalArgumentException.class, () -> new DeltaFragmentManager(0));
        assertThrows(IllegalArgumentException.class, () -> new DeltaFragmentManager(-1));
    }

    @Test
    void fragment_throwsOnNullArguments() {
        assertThrows(NullPointerException.class, () -> manager.fragment(null, new byte[10]));
        assertThrows(NullPointerException.class, () -> manager.fragment("s", null));
    }
}
