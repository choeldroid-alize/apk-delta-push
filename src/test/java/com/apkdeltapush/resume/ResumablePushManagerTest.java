package com.apkdeltapush.resume;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class ResumablePushManagerTest {

    private ResumablePushManager manager;

    @BeforeEach
    void setUp() {
        manager = new ResumablePushManager();
    }

    @Test
    void testSaveAndFindToken() {
        ResumeToken token = new ResumeToken("dev1", "com.app", 100L, 500L, "chk1");
        manager.saveToken(token);
        Optional<ResumeToken> found = manager.findToken("dev1", "com.app");
        assertTrue(found.isPresent());
        assertEquals(100L, found.get().getBytesTransferred());
    }

    @Test
    void testFindTokenNotPresent() {
        assertFalse(manager.findToken("unknown", "com.app").isPresent());
    }

    @Test
    void testHasToken() {
        ResumeToken token = new ResumeToken("dev1", "com.app", 0L, 200L, "chk");
        manager.saveToken(token);
        assertTrue(manager.hasToken("dev1", "com.app"));
        assertFalse(manager.hasToken("dev2", "com.app"));
    }

    @Test
    void testClearToken() {
        ResumeToken token = new ResumeToken("dev1", "com.app", 0L, 200L, "chk");
        manager.saveToken(token);
        manager.clearToken("dev1", "com.app");
        assertFalse(manager.hasToken("dev1", "com.app"));
    }

    @Test
    void testUpdateProgress() {
        ResumeToken token = new ResumeToken("dev1", "com.app", 100L, 1000L, "chk");
        manager.saveToken(token);
        ResumeToken updated = manager.updateProgress("dev1", "com.app", 600L);
        assertEquals(600L, updated.getBytesTransferred());
        assertEquals(1000L, updated.getTotalBytes());
    }

    @Test
    void testUpdateProgressThrowsIfNoToken() {
        assertThrows(IllegalStateException.class,
                () -> manager.updateProgress("ghost", "com.app", 100L));
    }

    @Test
    void testSaveNullTokenThrows() {
        assertThrows(IllegalArgumentException.class, () -> manager.saveToken(null));
    }

    @Test
    void testClearAllTokens() {
        manager.saveToken(new ResumeToken("d1", "p1", 0L, 100L, "c"));
        manager.saveToken(new ResumeToken("d2", "p2", 0L, 100L, "c"));
        manager.clearAllTokens();
        assertEquals(0, manager.getActiveTokenCount());
    }
}
