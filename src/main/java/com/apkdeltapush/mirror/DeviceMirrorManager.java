package com.apkdeltapush.mirror;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Mirrors an APK delta push from a source device to one or more target devices
 * using the configured concurrency and failure policy.
 */
public class DeviceMirrorManager {

    private static final Logger LOG = Logger.getLogger(DeviceMirrorManager.class.getName());

    private final MirrorPushDelegate pushDelegate;

    public DeviceMirrorManager(MirrorPushDelegate pushDelegate) {
        this.pushDelegate = Objects.requireNonNull(pushDelegate, "pushDelegate must not be null");
    }

    /**
     * Executes a mirror push according to the supplied config.
     *
     * @param apkPath  local path to the APK to push
     * @param config   mirror configuration
     * @return         aggregated result for all target devices
     */
    public DeviceMirrorResult mirror(String apkPath, DeviceMirrorConfig config) {
        Objects.requireNonNull(apkPath, "apkPath must not be null");
        Objects.requireNonNull(config, "config must not be null");

        List<String> targets = config.getTargetDeviceIds();
        if (targets.isEmpty()) {
            LOG.warning("No target devices specified for mirror push.");
            return DeviceMirrorResult.empty(config.getSourceDeviceId());
        }

        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(config.getMaxConcurrentMirrors(), targets.size()));

        Map<String, Boolean> results = new ConcurrentHashMap<>();
        List<Future<?>> futures = new ArrayList<>();

        for (String targetId : targets) {
            futures.add(executor.submit(() -> {
                try {
                    boolean skipped = config.isSkipIdenticalVersions() &&
                            pushDelegate.hasSameVersion(config.getSourceDeviceId(), targetId, apkPath);
                    if (skipped) {
                        LOG.info("Skipping " + targetId + " — identical version already installed.");
                        results.put(targetId, true);
                        return;
                    }
                    boolean success = pushDelegate.pushDelta(apkPath, config.getSourceDeviceId(), targetId);
                    results.put(targetId, success);
                    if (!success && config.isFailFast()) {
                        executor.shutdownNow();
                    }
                } catch (Exception e) {
                    LOG.severe("Mirror push to " + targetId + " failed: " + e.getMessage());
                    results.put(targetId, false);
                    if (config.isFailFast()) {
                        executor.shutdownNow();
                    }
                }
            }));
        }

        awaitAll(futures);
        executor.shutdown();

        return DeviceMirrorResult.of(config.getSourceDeviceId(), results);
    }

    private void awaitAll(List<Future<?>> futures) {
        for (Future<?> f : futures) {
            try { f.get(); } catch (Exception ignored) { /* individual errors captured in results map */ }
        }
    }
}
