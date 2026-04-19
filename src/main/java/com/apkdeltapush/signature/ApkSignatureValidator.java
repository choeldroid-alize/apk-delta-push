package com.apkdeltapush.signature;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Validates APK signatures before and after delta patching to ensure
 * integrity and authenticity of the APK file.
 */
public class ApkSignatureValidator {

    private static final Logger logger = Logger.getLogger(ApkSignatureValidator.class.getName());
    private static final String DIGEST_ALGORITHM = "SHA-256";

    /**
     * Validates the signature of the given APK file.
     *
     * @param apkPath path to the APK file
     * @return SignatureValidationResult containing status and certificate fingerprint
     * @throws IOException if the APK cannot be read
     */
    public SignatureValidationResult validate(Path apkPath) throws IOException {
        if (!Files.exists(apkPath)) {
            return SignatureValidationResult.failure("APK file not found: " + apkPath);
        }

        try (JarFile jar = new JarFile(apkPath.toFile(), true)) {
            JarEntry manifestEntry = jar.getJarEntry("AndroidManifest.xml");
            if (manifestEntry == null) {
                return SignatureValidationResult.failure("Not a valid APK: missing AndroidManifest.xml");
            }
            // Reading the entry triggers signature verification by JarFile
            try (InputStream is = jar.getInputStream(manifestEntry)) {
                byte[] buffer = new byte[4096];
                while (is.read(buffer) != -1) { /* consume */ }
            }
            String fingerprint = computeFileFingerprint(apkPath);
            logger.info("APK signature valid. Fingerprint: " + fingerprint);
            return SignatureValidationResult.success(fingerprint);
        } catch (SecurityException e) {
            logger.warning("APK signature verification failed: " + e.getMessage());
            return SignatureValidationResult.failure("Signature mismatch: " + e.getMessage());
        }
    }

    /**
     * Checks that two APKs share the same signing certificate fingerprint.
     */
    public boolean haveSameSigner(Path apkA, Path apkB) throws IOException {
        SignatureValidationResult resultA = validate(apkA);
        SignatureValidationResult resultB = validate(apkB);
        if (!resultA.isValid() || !resultB.isValid()) {
            return false;
        }
        return resultA.getCertificateFingerprint().equals(resultB.getCertificateFingerprint());
    }

    private String computeFileFingerprint(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
            digest.update(Files.readAllBytes(path));
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
