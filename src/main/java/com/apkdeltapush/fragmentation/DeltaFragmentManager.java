package com.apkdeltapush.fragmentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Splits a delta payload into fixed-size fragments for chunked transfer over ADB,
 * and reassembles them on demand.
 */
public class DeltaFragmentManager {

    private static final int DEFAULT_FRAGMENT_SIZE_BYTES = 512 * 1024; // 512 KB

    private final int fragmentSizeBytes;

    public DeltaFragmentManager() {
        this(DEFAULT_FRAGMENT_SIZE_BYTES);
    }

    public DeltaFragmentManager(int fragmentSizeBytes) {
        if (fragmentSizeBytes <= 0) {
            throw new IllegalArgumentException("Fragment size must be positive, got: " + fragmentSizeBytes);
        }
        this.fragmentSizeBytes = fragmentSizeBytes;
    }

    /**
     * Splits {@code data} into a list of {@link DeltaFragment} objects.
     *
     * @param sessionId unique push-session identifier
     * @param data      full delta payload bytes
     * @return ordered, immutable list of fragments
     */
    public List<DeltaFragment> fragment(String sessionId, byte[] data) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(data, "data must not be null");

        List<DeltaFragment> fragments = new ArrayList<>();
        int totalFragments = (int) Math.ceil((double) data.length / fragmentSizeBytes);
        if (totalFragments == 0) totalFragments = 1;

        for (int index = 0; index < totalFragments; index++) {
            int start = index * fragmentSizeBytes;
            int end = Math.min(start + fragmentSizeBytes, data.length);
            byte[] chunk = new byte[end - start];
            System.arraycopy(data, start, chunk, 0, chunk.length);
            fragments.add(new DeltaFragment(sessionId, index, totalFragments, chunk));
        }
        return Collections.unmodifiableList(fragments);
    }

    /**
     * Reassembles an ordered list of fragments back into the original byte array.
     *
     * @param fragments ordered list of fragments
     * @return reassembled payload
     * @throws IllegalArgumentException if the fragment list is null, empty, or contains gaps
     */
    public byte[] reassemble(List<DeltaFragment> fragments) {
        if (fragments == null || fragments.isEmpty()) {
            throw new IllegalArgumentException("Fragment list must not be null or empty");
        }

        int totalSize = fragments.stream().mapToInt(f -> f.getData().length).sum();
        byte[] result = new byte[totalSize];
        int offset = 0;

        List<DeltaFragment> sorted = new ArrayList<>(fragments);
        sorted.sort(java.util.Comparator.comparingInt(DeltaFragment::getIndex));

        for (int i = 0; i < sorted.size(); i++) {
            DeltaFragment fragment = sorted.get(i);
            if (fragment.getIndex() != i) {
                throw new IllegalArgumentException(
                        "Missing fragment at index " + i + " for session " + fragment.getSessionId());
            }
            System.arraycopy(fragment.getData(), 0, result, offset, fragment.getData().length);
            offset += fragment.getData().length;
        }
        return result;
    }

    public int getFragmentSizeBytes() {
        return fragmentSizeBytes;
    }
}
