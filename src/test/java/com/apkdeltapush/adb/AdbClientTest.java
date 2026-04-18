package com.apkdeltapush.adb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdbClientTest {

    /**
     * Unit test: listDevices() parses 'adb devices' output correctly.
     * Uses a subclass that stubs runCommand via a fake adb script.
     */
    @Test
    void listDevices_parsesOutputCorrectly() throws Exception {
        // Write a tiny fake 'adb' script that prints a fixed device list
        Path fakeAdb = writeFakeAdb(
                "List of devices attached\n"
                + "emulator-5554\tdevice\n"
                + "R3CN20WXYZ\tdevice\n"
                + "offline-device\toffline\n"
        );

        AdbClient client = new AdbClient(fakeAdb.toAbsolutePath().toString());
        List<String> devices = client.listDevices();

        assertEquals(2, devices.size(), "Should only count 'device' state entries");
        assertTrue(devices.contains("emulator-5554"));
        assertTrue(devices.contains("R3CN20WXYZ"));
        assertFalse(devices.contains("offline-device"));
    }

    @Test
    void listDevices_returnsEmptyWhenNoDevices() throws Exception {
        Path fakeAdb = writeFakeAdb("List of devices attached\n");
        AdbClient client = new AdbClient(fakeAdb.toAbsolutePath().toString());
        List<String> devices = client.listDevices();
        assertTrue(devices.isEmpty());
    }

    @Test
    void runCommand_throwsOnNonZeroExit() throws Exception {
        Path fakeAdb = writeFakeAdbWithExit("error output", 1);
        AdbClient client = new AdbClient(fakeAdb.toAbsolutePath().toString());
        assertThrows(IOException.class, client::listDevices);
    }

    // -------------------------------------------------------------------------

    private Path writeFakeAdb(String stdout) throws IOException {
        return writeFakeAdbWithExit(stdout, 0);
    }

    private Path writeFakeAdbWithExit(String stdout, int exitCode) throws IOException {
        Path script = Files.createTempFile("fake-adb-", ".sh");
        String content = "#!/bin/sh\nprintf '%s'\n" +
                "printf '" + stdout.replace("'", "'\\''")
                        .replace("\n", "\\n'") + "\n" +
                "exit " + exitCode + "\n";
        // Simpler portable approach:
        String scriptContent = "#!/bin/sh\n"
                + "cat <<'ENDOFOUTPUT'\n"
                + stdout
                + "ENDOFOUTPUT\n"
                + "exit " + exitCode + "\n";
        Files.writeString(script, scriptContent);
        script.toFile().setExecutable(true);
        return script;
    }
}
