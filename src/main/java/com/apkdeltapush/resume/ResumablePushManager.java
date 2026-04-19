package com.apkdeltapush.resume;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages resumable push sessions, allowing interrupted transfers to continue.
 */
public class ResumablePushManager {

    private final Map<String, ResumeToken> tokenStore = new ConcurrentHashMap<>();

    private String tokenKey(String deviceId, String packageName) {
        return deviceId + ":" + packageName;
    }

    public void saveToken(ResumeToken token) {
        if (token == null) throw new IllegalArgumentException("Token must not be null");
        tokenStore.put(tokenKey(token.getDeviceId(), token.getPackageName()), token);
    }

    public Optional<ResumeToken> findToken(String deviceId, String packageName) {
        return Optional.ofNullable(tokenStore.get(tokenKey(deviceId, packageName)));
    }

    public boolean hasToken(String deviceId, String packageName) {
        return tokenStore.containsKey(tokenKey(deviceId, packageName));
    }

    public void clearToken(String deviceId, String packageName) {
        tokenStore.remove(tokenKey(deviceId, packageName));
    }

    public void clearAllTokens() {
        tokenStore.clear();
    }

    public ResumeToken updateProgress(String deviceId, String packageName,
                                       long newBytesTransferred) {
        ResumeToken existing = tokenStore.get(tokenKey(deviceId, packageName));
        if (existing == null) {
            throw new IllegalStateException("No token found for " + deviceId + ":" + packageName);
        }
        ResumeToken updated = new ResumeToken(
                existing.getDeviceId(),
                existing.getPackageName(),
                newBytesTransferred,
                existing.getTotalBytes(),
                existing.getPatchChecksum()
        );
        saveToken(updated);
        return updated;
    }

    public int getActiveTokenCount() {
        return tokenStore.size();
    }
}
