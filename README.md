# VAJRA — Android Cyber Deck Launcher
**V0.1 — Codename: Tyranitar**

Vajra is a lightweight, tactical Android cyber deck launcher engineered for real-time hardware telemetry and modular, controlled security tool orchestration on mobile devices.

---

## 1. Supported Platform
- **OS**: Android 10+ (API 29+)
- **Primary Reference Hardware**: Samsung Galaxy J6+ (MSM8917 Snapdragon 425, 3GB RAM, 720x1480)
- **UI Framework**: Native Android XML Views (Zero Compose, Zero Flutter, Zero WebView)
- **Language**: Kotlin 2.1+ / Java 21

---

## 2. System Architecture
Vajra operates as a high-efficiency native Android launcher application structured in distinct operational planes:

```
+-------------------------------------------------------------+
|                     VAJRA CYBER DECK                        |
+-------------------------------------------------------------+
|  HOME        |  CYBER          |  APPS        |  SYSTEM     |
|  Telemetry   |  Tool Registry  |  Installed   |  Hardware   |
|  Dashboard   |  & Orchestration|  Applications|  & Device   |
+--------------+-----------------+--------------+-------------+
                        |
                        v
          +---------------------------+
          |      Security Boundary    |
          |  (TargetValidator & IPC)  |
          +---------------------------+
                        |
                        v
          +---------------------------+
          |       Termux Bridge       |
          |   (RunCommandService IPC) |
          +---------------------------+
                        |
            +-----------+-----------+
            |                       |
            v                       v
     +--------------+       +---------------+
     | Termux Native|       | Debian PRoot  |
     | Userspace    |       | Userspace     |
     +--------------+       +---------------+
            |                       |
            v                       v
     +--------------+       +---------------+
     | Native Tools |       | Nmap / CLI    |
     +--------------+       +---------------+
```

---

## 3. Environment Architecture
Vajra centralizes environmental state discovery within `EnvironmentManager`, resolving capabilities across three tiers:
1. **Android Userspace**: Native hardware telemetry (CPU via `/proc/stat`, RAM via `ActivityManager`, battery health/temperature via `BatteryManager`, and network state via `ConnectivityManager`). Zero fake fallbacks.
2. **Termux Userspace**: Detected via package availability and IPC permissions (`com.termux.permission.RUN_COMMAND`). Provides the bridge between the Android sandbox and POSIX tooling.
3. **Debian PRoot**: Containerized Linux distribution managed through Termux's `proot-distro`. Hosts full Linux security binaries without requiring root.

---

## 4. Security Model & Boundaries
Vajra enforces strict boundaries to protect device integrity:
- **Zero Root Execution**: Vajra executes completely without `su`, `root`, or custom kernel exploits.
- **Zero Raw Shell Direct Execution**: No direct `Runtime.getRuntime().exec()`, no `ProcessBuilder()`, and no raw shell strings executed from UI input.
- **Target Sanitization & Validation**: `TargetValidator` strictly rejects command chaining, pipes, redirection, subshells, metacharacters, and whitespace (e.g. `;`, `&`, `|`, `` ` ``, `$()`, `<`, `>`, `\n`). Supports IPv4 (including CIDR subnets like `/24`), valid hostnames, and IPv6.
- **Structured IPC**: All tool actions are tokenized into discrete argument arrays (`execvp` semantic delivery via `EXTRA_ARGUMENTS`), preventing shell injection.
- **Intent Hardening**: Results from Termux are returned via an explicit `PendingIntent` targeted directly to `TermuxResultReceiver`.

---

## 5. Default Launcher Behavior & Safety
Vajra registers as an Android Home launcher (`CATEGORY_HOME` and `CATEGORY_DEFAULT`):
- **Back Navigation Persistence**: Pressing the Back button on the Home screen is consumed to maintain the launcher context, preventing accidental launcher exits.
- **Hierarchical Back Unwinding**: Sub-screens unwind in reverse navigation order (`ToolConfig` -> `ToolDetails` -> `ToolList` -> `CyberCategories` -> `Home`).
- **Launcher Handoff**: The **Apps** tab features a dedicated `[Open Default Android Launcher]` card allowing clean, safe handoff to Samsung One UI or alternate system launchers without locking the user out.

---

## 6. Current Tool Execution Support (V0.1)
- **Nmap**: Full integration via Debian PRoot. Dynamic installation detection, version capture (`7.93+`), asynchronous Quick Scan, stdout/stderr extraction, exit code tracking, and execution duration reporting.
- **Dynamic Binary Discovery**: Real-time checking of binaries in Debian PRoot and Termux. Uninstalled tools display the **Tactical Install Guide** with copyable one-click installation commands and live re-checking.

---

## 7. V0.1 Limitations & Out of Scope
The following capabilities are deliberately excluded from V0.1:
- Automated password cracking suites (Hydra/John)
- Raw packet injection / monitor mode wireless workflows
- Automated exploitation engines (Metasploit)
- Cloud backends, account sync, or remote daemon orchestration

---

## 8. Building & Installation

### Prerequisites
- Android SDK (API 35 compileSdk, API 29 minSdk)
- JDK 21
- Gradle 8.11+ (via included `gradlew`)

### Build Commands
```bash
# Run automated unit tests (including TargetValidator injection tests)
./gradlew test

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```

### Installation via ADB
```bash
# Connect to target device over Wi-Fi or USB
adb connect <device-ip>:<port>

# Install Debug APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch Vajra
adb shell am start -n com.vajra.launcher/.MainActivity
```

---

## 9. Verification & Testing Procedure
1. **Cold Start**: Launch Vajra and verify the initialization sequence transitioning to the Home dashboard.
2. **Telemetry**: Confirm CPU load, RAM usage, Battery health, Temperature, and Network state are dynamic (or `"Unavailable"` if hardware sensor is inaccessible).
3. **Cyber Navigation**: Navigate Cyber -> Recon -> Nmap. Confirm dynamic "Installed" badge and version display.
4. **Nmap Quick Scan**: Run a Quick Scan against an authorized local IP (e.g. `127.0.0.1` or subnet `192.168.1.0/24`). Confirm real-time progress, exit code 0, and duration.
5. **Security Injection Test**: Input `127.0.0.1;whoami` or `127.0.0.1 && id` into the scan target dialog. Confirm immediate validation rejection.
6. **Missing Tool Guidance**: Open an uninstalled tool (e.g. Subfinder). Verify "Not installed" badge, install dialog, and command copying.
7. **Apps & Handoff**: Search and launch an installed app; return to Vajra; test launcher handoff to default Android launcher.
8. **Themes**: Toggle Dark, Light, and AMOLED themes in Customization to verify color contrast and typography.
