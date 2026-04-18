# apk-delta-push

> CLI tool to diff and push incremental APK updates to connected Android devices over ADB

---

## Installation

**Requirements:** Java 11+, ADB installed and available in your `PATH`

```bash
git clone https://github.com/youruser/apk-delta-push.git
cd apk-delta-push && ./mvnw package -q
```

---

## Usage

```bash
java -jar apk-delta-push.jar [OPTIONS] <old-apk> <new-apk>
```

### Examples

Push an incremental update to all connected devices:
```bash
java -jar apk-delta-push.jar app-v1.apk app-v2.apk
```

Target a specific device by serial:
```bash
java -jar apk-delta-push.jar --device emulator-5554 app-v1.apk app-v2.apk
```

Force a full reinstall instead of a delta push:
```bash
java -jar apk-delta-push.jar --full-install app-v1.apk app-v2.apk
```

### Options

| Flag | Description |
|------|-------------|
| `--device <serial>` | Target a specific ADB device serial |
| `--full-install` | Skip diffing and push the full APK |
| `--dry-run` | Compute and display the delta without pushing |
| `--verbose` | Enable verbose logging output |

---

## How It Works

`apk-delta-push` computes a binary delta between two APK files using a block-level diff algorithm, transfers only the changed bytes to the device via ADB, and applies the patch in-place — significantly reducing transfer time for incremental builds.

---

## Contributing

Pull requests are welcome. Please open an issue first to discuss any significant changes.

---

## License

This project is licensed under the [MIT License](LICENSE).