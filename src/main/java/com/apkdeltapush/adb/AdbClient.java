package com.apkdeltapush.adb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Thin wrapper around the ADB command-line tool.
 * Discovers connected devices and executes basic ADB commands.
 */
public class AdbClient {

    private static final Logger LOG = Logger.getLogger(AdbClient.class.getName());

    private final String adbPath;

    public AdbClient(String adbPath) {
        this.adbPath = adbPath;
    }

    public AdbClient() {
        this("adb");
    }

    /**
     * Returns serial numbers of all currently connected devices.
     */
    public List<String> listDevices() throws IOException, InterruptedException {
        List<String> devices = new ArrayList<>();
        List<String> lines = runCommand(adbPath, "devices");
        for (String line : lines) {
            if (line.endsWith("\tdevice")) {
                devices.add(line.split("\t")[0].trim());
            }
        }
        return devices;
    }

    /**
     * Pushes a local file to a remote path on the specified device.
     */
    public void push(String serial, String localPath, String remotePath)
            throws IOException, InterruptedException {
        LOG.info(String.format("[%s] Pushing %s -> %s", serial, localPath, remotePath));
        runCommand(adbPath, "-s", serial, "push", localPath, remotePath);
    }

    /**
     * Executes a shell command on the specified device and returns stdout lines.
     */
    public List<String> shell(String serial, String... shellArgs)
            throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        cmd.add(adbPath);
        cmd.add("-s");
        cmd.add(serial);
        cmd.add("shell");
        for (String arg : shellArgs) cmd.add(arg);
        return runCommand(cmd.toArray(new String[0]));
    }

    private List<String> runCommand(String... args) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        List<String> output = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("ADB command failed (exit " + exitCode + "): "
                    + String.join(" ", args) + "\nOutput: " + output);
        }
        return output;
    }
}
