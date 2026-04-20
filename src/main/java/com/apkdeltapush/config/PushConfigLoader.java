package com.apkdeltapush.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Loads {@link PushConfig} from a .properties file or from system properties / env vars.
 * Supports layered loading: file → system properties → env vars (highest priority).
 */
public class PushConfigLoader {

    private static final Logger LOGGER = Logger.getLogger(PushConfigLoader.class.getName());
    private static final String ENV_PREFIX = "APK_PUSH_";

    /**
     * Loads config from the given properties file path, then overlays
     * matching system properties and environment variables.
     */
    public PushConfig load(Path configFilePath) throws IOException {
        Map<String, String> merged = new HashMap<>();

        if (configFilePath != null && Files.exists(configFilePath)) {
            Properties fileProps = new Properties();
            try (InputStream is = Files.newInputStream(configFilePath)) {
                fileProps.load(is);
            }
            for (String name : fileProps.stringPropertyNames()) {
                merged.put(name, fileProps.getProperty(name));
            }
            LOGGER.fine("Loaded config from file: " + configFilePath);
        } else if (configFilePath != null) {
            LOGGER.warning("Config file not found, using defaults: " + configFilePath);
        }

        // Overlay system properties that start with "push."
        for (String key : System.getProperties().stringPropertyNames()) {
            if (key.startsWith("push.")) {
                merged.put(key, System.getProperty(key));
            }
        }

        // Overlay environment variables prefixed with APK_PUSH_
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            if (entry.getKey().startsWith(ENV_PREFIX)) {
                String configKey = envKeyToConfigKey(entry.getKey());
                merged.put(configKey, entry.getValue());
            }
        }

        return new PushConfig(merged);
    }

    /** Loads a default (empty) config with no file source. */
    public PushConfig loadDefaults() {
        try {
            return load(null);
        } catch (IOException e) {
            return new PushConfig();
        }
    }

    /** Converts APK_PUSH_MAX_RETRIES → push.max_retries */
    private String envKeyToConfigKey(String envKey) {
        return envKey.substring(ENV_PREFIX.length())
                     .toLowerCase()
                     .replace('_', '.');
    }
}
