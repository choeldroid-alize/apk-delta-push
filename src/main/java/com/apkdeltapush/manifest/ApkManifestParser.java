package com.apkdeltapush.manifest;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Parses basic manifest attributes from an APK file.
 */
public class ApkManifestParser {

    /**
     * Parses the APK at the given path and returns a ManifestInfo.
     *
     * @param apkPath path to the APK file
     * @return parsed ManifestInfo
     * @throws IOException if the file cannot be read or AndroidManifest.xml is missing
     */
    public ManifestInfo parse(String apkPath) throws IOException {
        File apkFile = new File(apkPath);
        if (!apkFile.exists() || !apkFile.isFile()) {
            throw new IOException("APK file not found: " + apkPath);
        }

        try (ZipFile zip = new ZipFile(apkFile)) {
            ZipEntry manifestEntry = zip.getEntry("AndroidManifest.xml");
            if (manifestEntry == null) {
                throw new IOException("AndroidManifest.xml not found in APK: " + apkPath);
            }

            long manifestSize = manifestEntry.getSize();
            int entryCount = countEntries(zip);
            String packageName = extractPackageName(apkPath);

            return new ManifestInfo(packageName, manifestSize, entryCount, apkFile.length());
        }
    }

    private int countEntries(ZipFile zip) {
        int count = 0;
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            entries.nextElement();
            count++;
        }
        return count;
    }

    /**
     * Derives a best-effort package name from the APK filename.
     * In production this would use binary XML parsing of AndroidManifest.xml.
     */
    private String extractPackageName(String apkPath) {
        String name = new File(apkPath).getName();
        if (name.endsWith(".apk")) {
            name = name.substring(0, name.length() - 4);
        }
        return name.replaceAll("[^a-zA-Z0-9.]", ".").toLowerCase();
    }
}
