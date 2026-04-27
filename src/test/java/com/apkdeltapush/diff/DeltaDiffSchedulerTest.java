package com.apkdeltapush.diff;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeltaDiffSchedulerTest {

    private DeltaDiffScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new DeltaDiffScheduler(2);
    }

    @AfterEach
    void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    void constructor_rejectsNonPositiveConcurrency() {
        assertThrows(IllegalArgumentException.class, () -> new DeltaDiffScheduler(0));
        assertThrows(IllegalArgumentException.class, () -> new DeltaDiffScheduler(-1));
    }

    @Test
    void getMaxConcurrentDiffs_returnsConfiguredValue() {
        assertEquals(2, scheduler.getMaxConcurrentDiffs());
    }

    @Test
    void availableSlots_initiallyEqualsMax() {
        assertEquals(2, scheduler.availableSlots());
    }

    @Test
    void submit_executesTaskAndReturnsResult(@TempDir Path tempDir) throws Exception {
        File src = createTempApk(tempDir, "source.apk");
        File tgt = createTempApk(tempDir, "target.apk");

        DeltaDiffResult mockResult = mock(DeltaDiffResult.class);
        DeltaDiffEngine mockEngine = mock(DeltaDiffEngine.class);
        DeltaDiffOptions options = mock(DeltaDiffOptions.class);

        when(mockEngine.computeDiff(src, tgt, options)).thenReturn(mockResult);

        DeltaDiffTask task = new DeltaDiffTask(src, tgt, options, mockEngine);
        Future<DeltaDiffResult> future = scheduler.submit(task);

        DeltaDiffResult result = future.get();
        assertSame(mockResult, result);
        verify(mockEngine, times(1)).computeDiff(src, tgt, options);
    }

    @Test
    void submit_afterShutdown_throwsRejectedExecutionException(@TempDir Path tempDir) throws Exception {
        File src = createTempApk(tempDir, "source.apk");
        File tgt = createTempApk(tempDir, "target.apk");
        DeltaDiffEngine mockEngine = mock(DeltaDiffEngine.class);
        DeltaDiffOptions options = mock(DeltaDiffOptions.class);

        scheduler.shutdown(1000);
        assertTrue(scheduler.isShutdown());

        DeltaDiffTask task = new DeltaDiffTask(src, tgt, options, mockEngine);
        assertThrows(RejectedExecutionException.class, () -> scheduler.submit(task));
    }

    @Test
    void submit_multipleTasksConcurrently(@TempDir Path tempDir) throws Exception {
        DeltaDiffScheduler wideScheduler = new DeltaDiffScheduler(4);
        List<Future<DeltaDiffResult>> futures = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            File src = createTempApk(tempDir, "source" + i + ".apk");
            File tgt = createTempApk(tempDir, "target" + i + ".apk");
            DeltaDiffEngine mockEngine = mock(DeltaDiffEngine.class);
            DeltaDiffOptions options = mock(DeltaDiffOptions.class);
            DeltaDiffResult mockResult = mock(DeltaDiffResult.class);
            when(mockEngine.computeDiff(src, tgt, options)).thenReturn(mockResult);
            futures.add(wideScheduler.submit(new DeltaDiffTask(src, tgt, options, mockEngine)));
        }

        for (Future<DeltaDiffResult> f : futures) {
            assertNotNull(f.get());
        }
        wideScheduler.shutdownNow();
    }

    private File createTempApk(Path dir, String name) throws IOException {
        File f = dir.resolve(name).toFile();
        assertTrue(f.createNewFile());
        return f;
    }
}
