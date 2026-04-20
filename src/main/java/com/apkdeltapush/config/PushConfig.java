package com.apkdeltapush.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Holds configuration parameters for an APK delta push operation.
 * Supports typed access and default value fallback.
 */
public class PushConfig {

    private final Map<String, String> properties;

    public static final String KEY_MAX_RETRIES = "push.max_retries";
    public static final String KEY_TIMEOUT_SECONDS = "push.timeout_seconds";
    public static final String KEY_BANDWIDTH_LIMIT_KBPS = "push.bandwidth_limit_kbps";
    public static final String KEY_DELTA_CACHE_ENABLED = "push.delta_cache_enabled";
    public static final String KEY_STAGING_DIR = "push.staging_dir";
    public static final String KEY_VERIFY_AFTER_PUSH = "push.verify_after_push";
    public static final String KEY_COMPRESS_DELTA = "push.compress_delta";

    public PushConfig(Map<String, String> properties) {
        this.properties = new HashMap<>(properties);
    }

    public PushConfig() {
        this.properties = new HashMap<>();
    }

    public void set(String key, String value) {
        properties.put(key, value);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(properties.get(key));
    }

    public String getOrDefault(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String val = properties.get(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String val = properties.get(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val.trim());
    }

    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    public Map<String, String> toMap() {
        return new HashMap<>(properties);
    }

    @Override
    public String toString() {
        return "PushConfig" + properties;
    }
}
