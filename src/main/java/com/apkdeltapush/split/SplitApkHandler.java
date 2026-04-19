package com.apkdeltapush.split;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles split APK sets (base + config splits) for delta push operations.
 */
public class SplitApkHandler {

    private static final Logger logger = Logger.getLogger(SplitApkHandler.class.getName());
    private static final String BASE_APK_PREFIX = "base";

    public SplitApkSet loadSplitSet(File apkSetDir) {
        if (apkSetDir == null || !apkSetDir.isDirectory()) {
            throw new IllegalArgumentException("apkSetDir must be a valid directory: " + apkSetDir);
        }

        File[] apkFiles = apkSetDir.listFiles((dir, name) -> name.endsWith(".apk"));
        if (apkFiles == null || apkFiles.length == 0) {
            throw new IllegalStateException("No APK files found in: " + apkSetDir.getAbsolutePath());
        }

        File baseApk = null;
        List<File> splitApks = new ArrayList<>();

        for (File f : apkFiles) {
            if (f.getName().startsWith(BASE_APK_PREFIX)) {
                baseApk = f;
            } else {
                splitApks.add(f);
            }
        }

        if (baseApk == null) {
            throw new IllegalStateException("No base APK found in split set directory: " + apkSetDir);
        }

        Collections.sort(splitApks, (a, b) -> a.getName().compareTo(b.getName()));
        logger.info("Loaded split APK set: base=" + baseApk.getName() + ", splits=" + splitApks.size());
        return new SplitApkSet(baseApk, splitApks);
    }

    public boolean isSplitApkDirectory(File dir) {
        if (dir == null || !dir.isDirectory()) return false;
        File[] apks = dir.listFiles((d, name) -> name.endsWith(".apk"));
        return apks != null && apks.length > 1;
    }

    public long totalSize(SplitApkSet set) {
        long total = set.getBaseApk().length();
        for (File split : set.getSplitApks()) {
            total += split.length();
        }
        return total;
    }

    public List<File> allApks(SplitApkSet set) {
        List<File> all = new ArrayList<>();
        all.add(set.getBaseApk());
        all.addAll(set.getSplitApks());
        return Collections.unmodifiableList(all);
    }
}
