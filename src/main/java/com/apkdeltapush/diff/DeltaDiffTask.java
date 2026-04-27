package com.apkdeltapush.diff;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single delta diff computation task between two APK files.
 * Encapsulates inputs, options, and execution logic.
 */
public class DeltaDiffTask {

    private final String taskId;
    private final File sourceApk;
    private final File targetApk;
    private final DeltaDiffOptions options;
    private final DeltaDiffEngine engine;

    public DeltaDiffTask(File sourceApk, File targetApk, DeltaDiffOptions options, DeltaDiffEngine engine) {
        Objects.requireNonNull(sourceApk, "sourceApk must not be null");
        Objects.requireNonNull(targetApk, "targetApk must not be null");
        Objects.requireNonNull(options, "options must not be null");
        Objects.requireNonNull(engine, "engine must not be null");
        this.taskId = UUID.randomUUID().toString();
        this.sourceApk = sourceApk;
        this.targetApk = targetApk;
        this.options = options;
        this.engine = engine;
    }

    /**
     * Executes the diff computation and returns the result.
     *
     * @return the computed DeltaDiffResult
     * @throws DeltaDiffException if the diff computation fails
     */
    public DeltaDiffResult execute() throws DeltaDiffException {
        if (!sourceApk.exists()) {
            throw new DeltaDiffException("Source APK not found: " + sourceApk.getAbsolutePath());
        }
        if (!targetApk.exists()) {
            throw new DeltaDiffException("Target APK not found: " + targetApk.getAbsolutePath());
        }
        return engine.computeDiff(sourceApk, targetApk, options);
    }

    public String getTaskId() {
        return taskId;
    }

    public File getSourceApk() {
        return sourceApk;
    }

    public File getTargetApk() {
        return targetApk;
    }

    public DeltaDiffOptions getOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "DeltaDiffTask{taskId='" + taskId + "', source='" + sourceApk.getName()
                + "', target='" + targetApk.getName() + "'}";
    }
}
