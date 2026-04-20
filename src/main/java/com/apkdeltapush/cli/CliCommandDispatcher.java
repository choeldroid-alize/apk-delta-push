package com.apkdeltapush.cli;

import com.apkdeltapush.adb.AdbClient;
import com.apkdeltapush.cache.DeltaCache;
import com.apkdeltapush.device.DeviceRegistry;
import com.apkdeltapush.diff.ApkDiffGenerator;
import com.apkdeltapush.push.DeltaPushManager;
import com.apkdeltapush.rollback.RollbackManager;
import com.apkdeltapush.session.PushSessionManager;
import com.apkdeltapush.verify.InstallVerifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Dispatches CLI commands to the appropriate handlers.
 * Acts as the central command router for the apk-delta-push CLI tool.
 *
 * <p>Supported commands:
 * <ul>
 *   <li>{@code push}    - Push an APK delta to one or more connected devices</li>
 *   <li>{@code diff}    - Generate a delta between two APK versions</li>
 *   <li>{@code devices} - List currently connected ADB devices</li>
 *   <li>{@code rollback}- Roll back the last pushed APK on a device</li>
 *   <li>{@code status}  - Show the current push session status</li>
 *   <li>{@code cache}   - Manage the local delta cache</li>
 *   <li>{@code help}    - Print usage information</li>
 * </ul>
 */
public class CliCommandDispatcher {

    private static final Logger LOGGER = Logger.getLogger(CliCommandDispatcher.class.getName());

    private final AdbClient adbClient;
    private final DeltaPushManager pushManager;
    private final ApkDiffGenerator diffGenerator;
    private final DeviceRegistry deviceRegistry;
    private final RollbackManager rollbackManager;
    private final PushSessionManager sessionManager;
    private final InstallVerifier installVerifier;
    private final DeltaCache deltaCache;

    /** Maps command names to their handler methods for fast dispatch. */
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();

    public CliCommandDispatcher(AdbClient adbClient,
                                DeltaPushManager pushManager,
                                ApkDiffGenerator diffGenerator,
                                DeviceRegistry deviceRegistry,
                                RollbackManager rollbackManager,
                                PushSessionManager sessionManager,
                                InstallVerifier installVerifier,
                                DeltaCache deltaCache) {
        this.adbClient = adbClient;
        this.pushManager = pushManager;
        this.diffGenerator = diffGenerator;
        this.deviceRegistry = deviceRegistry;
        this.rollbackManager = rollbackManager;
        this.sessionManager = sessionManager;
        this.installVerifier = installVerifier;
        this.deltaCache = deltaCache;
        registerHandlers();
    }

    private void registerHandlers() {
        commandHandlers.put("push",     args -> handlePush(args));
        commandHandlers.put("diff",     args -> handleDiff(args));
        commandHandlers.put("devices",  args -> handleDevices(args));
        commandHandlers.put("rollback", args -> handleRollback(args));
        commandHandlers.put("status",   args -> handleStatus(args));
        commandHandlers.put("cache",    args -> handleCache(args));
        commandHandlers.put("help",     args -> handleHelp(args));
    }

    /**
     * Dispatches the given CLI arguments to the appropriate command handler.
     *
     * @param args raw CLI argument array (e.g. from {@code main(String[])})
     * @return exit code: 0 for success, non-zero for failure
     */
    public int dispatch(String[] args) {
        if (args == null || args.length == 0) {
            handleHelp(List.of());
            return 1;
        }

        String command = args[0].toLowerCase();
        List<String> remaining = Arrays.asList(args).subList(1, args.length);

        CommandHandler handler = commandHandlers.get(command);
        if (handler == null) {
            System.err.println("Unknown command: '" + command + "'. Run 'apk-delta-push help' for usage.");
            return 1;
        }

        try {
            return handler.execute(remaining);
        } catch (Exception e) {
            LOGGER.severe("Command '" + command + "' failed: " + e.getMessage());
            System.err.println("Error: " + e.getMessage());
            return 2;
        }
    }

    // -------------------------------------------------------------------------
    // Command handlers
    // -------------------------------------------------------------------------

    private int handlePush(List<String> args) {
        if (args.size() < 2) {
            System.err.println("Usage: push <old-apk> <new-apk> [--device <serial>]");
            return 1;
        }
        String oldApk = args.get(0);
        String newApk = args.get(1);
        String deviceSerial = extractOption(args, "--device");
        LOGGER.info("Dispatching push: " + oldApk + " -> " + newApk
                + (deviceSerial != null ? " on " + deviceSerial : " on all devices"));
        pushManager.push(oldApk, newApk, deviceSerial);
        System.out.println("Push completed successfully.");
        return 0;
    }

    private int handleDiff(List<String> args) {
        if (args.size() < 2) {
            System.err.println("Usage: diff <old-apk> <new-apk> [--output <patch-file>]");
            return 1;
        }
        String oldApk = args.get(0);
        String newApk = args.get(1);
        String output = extractOption(args, "--output");
        String patchPath = diffGenerator.generate(oldApk, newApk, output);
        System.out.println("Delta patch written to: " + patchPath);
        return 0;
    }

    private int handleDevices(List<String> args) {
        List<String> devices = deviceRegistry.listConnectedDevices();
        if (devices.isEmpty()) {
            System.out.println("No devices connected.");
        } else {
            System.out.println("Connected devices ("+devices.size()+"):");
            devices.forEach(d -> System.out.println("  " + d));
        }
        return 0;
    }

    private int handleRollback(List<String> args) {
        String deviceSerial = extractOption(args, "--device");
        if (deviceSerial == null && !args.isEmpty()) {
            deviceSerial = args.get(0);
        }
        if (deviceSerial == null) {
            System.err.println("Usage: rollback <device-serial> | --device <serial>");
            return 1;
        }
        rollbackManager.rollback(deviceSerial);
        System.out.println("Rollback completed for device: " + deviceSerial);
        return 0;
    }

    private int handleStatus(List<String> args) {
        String sessionId = extractOption(args, "--session");
        sessionManager.printStatus(sessionId);
        return 0;
    }

    private int handleCache(List<String> args) {
        String subCmd = args.isEmpty() ? "list" : args.get(0);
        switch (subCmd) {
            case "clear":
                deltaCache.clear();
                System.out.println("Delta cache cleared.");
                break;
            case "list":
                deltaCache.listEntries().forEach(e -> System.out.println("  " + e));
                break;
            default:
                System.err.println("Unknown cache sub-command: " + subCmd + ". Use 'list' or 'clear'.");
                return 1;
        }
        return 0;
    }

    private int handleHelp(List<String> args) {
        System.out.println("apk-delta-push — incremental APK update tool");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  push     <old-apk> <new-apk> [--device <serial>]  Push delta to device(s)");
        System.out.println("  diff     <old-apk> <new-apk> [--output <file>]    Generate delta patch");
        System.out.println("  devices                                            List connected devices");
        System.out.println("  rollback <device-serial>                           Roll back last push");
        System.out.println("  status   [--session <id>]                          Show push session status");
        System.out.println("  cache    [list|clear]                              Manage local delta cache");
        System.out.println("  help                                               Show this message");
        return 0;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Extracts the value following a named option flag from the argument list.
     *
     * @param args argument list
     * @param flag option flag (e.g. {@code "--device"})
     * @return the value after the flag, or {@code null} if not present
     */
    private String extractOption(List<String> args, String flag) {
        for (int i = 0; i < args.size() - 1; i++) {
            if (args.get(i).equalsIgnoreCase(flag)) {
                return args.get(i + 1);
            }
        }
        return null;
    }

    /** Functional interface for command handler lambdas. */
    @FunctionalInterface
    private interface CommandHandler {
        int execute(List<String> args) throws Exception;
    }
}
