package com.apkdeltapush.pipeline;

import com.apkdeltapush.adb.AdbClient;
import com.apkdeltapush.checksum.ChecksumVerifier;
import com.apkdeltapush.compress.DeltaCompressor;
import com.apkdeltapush.diff.ApkDiffGenerator;
import com.apkdeltapush.encrypt.DeltaEncryptor;
import com.apkdeltapush.patch.PatchApplier;
import com.apkdeltapush.patch.PatchValidator;
import com.apkdeltapush.progress.PushProgressTracker;
import com.apkdeltapush.progress.PushPhase;
import com.apkdeltapush.retry.RetryPolicy;
import com.apkdeltapush.verify.InstallVerifier;
import com.apkdeltapush.verify.VerificationResult;

import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Orchestrates the full end-to-end delta push pipeline for a single APK update.
 *
 * <p>Stages executed in order:
 * <ol>
 *   <li>Diff generation – compute binary delta between old and new APK</li>
 *   <li>Compression – reduce delta size before transfer</li>
 *   <li>Encryption – optionally encrypt the delta payload</li>
 *   <li>Checksum – attach integrity checksum to the payload</li>
 *   <li>Transfer – push payload to the device via ADB</li>
 *   <li>Patch application – apply delta on-device</li>
 *   <li>Validation – verify the resulting APK is intact</li>
 *   <li>Install verification – confirm the package is correctly installed</li>
 * </ol>
 */
public class PushPipeline {

    private static final Logger LOG = Logger.getLogger(PushPipeline.class.getName());

    private final ApkDiffGenerator diffGenerator;
    private final DeltaCompressor compressor;
    private final DeltaEncryptor encryptor;
    private final ChecksumVerifier checksumVerifier;
    private final AdbClient adbClient;
    private final PatchValidator patchValidator;
    private final PatchApplier patchApplier;
    private final InstallVerifier installVerifier;
    private final PushProgressTracker progressTracker;
    private final RetryPolicy retryPolicy;

    public PushPipeline(ApkDiffGenerator diffGenerator,
                        DeltaCompressor compressor,
                        DeltaEncryptor encryptor,
                        ChecksumVerifier checksumVerifier,
                        AdbClient adbClient,
                        PatchValidator patchValidator,
                        PatchApplier patchApplier,
                        InstallVerifier installVerifier,
                        PushProgressTracker progressTracker,
                        RetryPolicy retryPolicy) {
        this.diffGenerator    = Objects.requireNonNull(diffGenerator,    "diffGenerator");
        this.compressor       = Objects.requireNonNull(compressor,       "compressor");
        this.encryptor        = Objects.requireNonNull(encryptor,        "encryptor");
        this.checksumVerifier = Objects.requireNonNull(checksumVerifier, "checksumVerifier");
        this.adbClient        = Objects.requireNonNull(adbClient,        "adbClient");
        this.patchValidator   = Objects.requireNonNull(patchValidator,   "patchValidator");
        this.patchApplier     = Objects.requireNonNull(patchApplier,     "patchApplier");
        this.installVerifier  = Objects.requireNonNull(installVerifier,  "installVerifier");
        this.progressTracker  = Objects.requireNonNull(progressTracker,  "progressTracker");
        this.retryPolicy      = Objects.requireNonNull(retryPolicy,      "retryPolicy");
    }

    /**
     * Executes the full push pipeline.
     *
     * @param context pipeline execution context carrying paths, device id, and options
     * @return a {@link PipelineResult} describing the outcome of each stage
     * @throws PipelineException if a non-recoverable failure occurs
     */
    public PipelineResult execute(PipelineContext context) throws PipelineException {
        Objects.requireNonNull(context, "context must not be null");
        LOG.info("Starting push pipeline for device " + context.getDeviceSerial()
                + ", package " + context.getPackageName());

        PipelineResult.Builder result = PipelineResult.builder()
                .deviceSerial(context.getDeviceSerial())
                .packageName(context.getPackageName());

        try {
            // Stage 1 – diff
            progressTracker.advance(PushPhase.DIFF);
            Path deltaPath = diffGenerator.generate(context.getBaseApkPath(), context.getTargetApkPath());
            LOG.fine("Delta generated: " + deltaPath);

            // Stage 2 – compress
            progressTracker.advance(PushPhase.COMPRESS);
            Path compressedPath = compressor.compress(deltaPath);
            LOG.fine("Delta compressed: " + compressedPath);

            // Stage 3 – encrypt (if enabled)
            Path payloadPath = compressedPath;
            if (context.isEncryptionEnabled()) {
                progressTracker.advance(PushPhase.ENCRYPT);
                payloadPath = encryptor.encrypt(compressedPath, context.getEncryptionKey());
                LOG.fine("Delta encrypted: " + payloadPath);
            }

            // Stage 4 – checksum
            progressTracker.advance(PushPhase.CHECKSUM);
            String checksum = checksumVerifier.computeChecksum(payloadPath);
            result.payloadChecksum(checksum);
            LOG.fine("Checksum computed: " + checksum);

            // Stage 5 – transfer with retry
            progressTracker.advance(PushPhase.TRANSFER);
            final Path finalPayload = payloadPath;
            retryPolicy.execute(() -> {
                adbClient.push(context.getDeviceSerial(), finalPayload, context.getRemoteStagingPath());
                return null;
            });
            LOG.fine("Payload transferred to " + context.getRemoteStagingPath());

            // Stage 6 – validate patch before applying
            progressTracker.advance(PushPhase.VALIDATE);
            boolean valid = patchValidator.validate(context.getDeviceSerial(),
                    context.getRemoteStagingPath(), checksum);
            if (!valid) {
                throw new PipelineException("Patch validation failed – checksum mismatch on device");
            }

            // Stage 7 – apply patch
            progressTracker.advance(PushPhase.APPLY);
            retryPolicy.execute(() -> {
                patchApplier.apply(context.getDeviceSerial(),
                        context.getRemoteStagingPath(),
                        context.getPackageName());
                return null;
            });
            LOG.fine("Patch applied successfully");

            // Stage 8 – verify installation
            progressTracker.advance(PushPhase.VERIFY);
            VerificationResult verification = installVerifier.verify(
                    context.getDeviceSerial(), context.getPackageName(),
                    context.getExpectedVersionCode());
            result.verificationResult(verification);

            if (!verification.isSuccess()) {
                throw new PipelineException("Install verification failed: " + verification.getFailureReason());
            }

            progressTracker.complete();
            LOG.info("Push pipeline completed successfully for " + context.getPackageName());
            return result.success(true).build();

        } catch (PipelineException pe) {
            progressTracker.fail(pe.getMessage());
            throw pe;
        } catch (Exception e) {
            progressTracker.fail(e.getMessage());
            throw new PipelineException("Unexpected pipeline failure: " + e.getMessage(), e);
        }
    }
}
