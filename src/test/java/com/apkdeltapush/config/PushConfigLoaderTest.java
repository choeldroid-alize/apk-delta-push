package com.apkdeltapush.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PushConfigLoaderTest {

    private final PushConfigLoader loader = new PushConfigLoader();

    @TempDir
    Path tempDir;

    @Test
    void loadFromFile_parsesPropertiesCorrectly() throws IOException {
        Path configFile = tempDir.resolve("push.properties");
        Files.writeString(configFile,
                "push.max_retries=5\n" +
                "push.timeout_seconds=30\n" +
                "push.delta_cache_enabled=true\n" +
                "push.staging_dir=/tmp/staging\n");

        PushConfig config = loader.load(configFile);

        assertEquals(5, config.getInt(PushConfig.KEY_MAX_RETRIES, 0));
        assertEquals(30, config.getInt(PushConfig.KEY_TIMEOUT_SECONDS, 0));
        assertTrue(config.getBoolean(PushConfig.KEY_DELTA_CACHE_ENABLED, false));
        assertEquals("/tmp/staging", config.getOrDefault(PushConfig.KEY_STAGING_DIR, ""));
    }

    @Test
    void loadFromMissingFile_returnsEmptyConfig() throws IOException {
        Path missing = tempDir.resolve("nonexistent.properties");
        PushConfig config = loader.load(missing);
        assertNotNull(config);
        assertEquals(3, config.getInt(PushConfig.KEY_MAX_RETRIES, 3));
    }

    @Test
    void loadDefaults_returnsNonNullConfig() {
        PushConfig config = loader.loadDefaults();
        assertNotNull(config);
    }

    @Test
    void getBoolean_returnsFalseForMissingKey() throws IOException {
        PushConfig config = loader.load(null);
        assertFalse(config.getBoolean(PushConfig.KEY_VERIFY_AFTER_PUSH, false));
    }

    @Test
    void set_and_get_roundtrip() {
        PushConfig config = new PushConfig();
        config.set(PushConfig.KEY_COMPRESS_DELTA, "true");
        Optional<String> val = config.get(PushConfig.KEY_COMPRESS_DELTA);
        assertTrue(val.isPresent());
        assertEquals("true", val.get());
    }

    @Test
    void getInt_withInvalidValue_returnsDefault() {
        PushConfig config = new PushConfig();
        config.set(PushConfig.KEY_BANDWIDTH_LIMIT_KBPS, "not-a-number");
        assertEquals(512, config.getInt(PushConfig.KEY_BANDWIDTH_LIMIT_KBPS, 512));
    }

    @Test
    void toMap_returnsCopyOfProperties() {
        PushConfig config = new PushConfig();
        config.set("push.staging_dir", "/data");
        var map = config.toMap();
        assertEquals("/data", map.get("push.staging_dir"));
        map.put("push.staging_dir", "modified");
        assertEquals("/data", config.getOrDefault("push.staging_dir", ""));
    }
}
