# F\*ck Tele (Native Kotlin Edition)

High-performance, ultra-low-size native Android app that hides ad SMS notifications without deleting the messages. Every SMS stays in your normal inbox. You choose what gets muted with allow and block rules.

100% native Kotlin — zero Expo, zero React Native, zero JavaScript runtime, and zero `INTERNET` permission for verifiable on-device privacy.

## Why Native Kotlin?

- **Ultra-low APK size**: Release APK is only **2.2 MB** (down from ~140 MB in React Native debug builds).
- **Near-zero RAM usage**: Minimal background memory footprint (~15 MB).
- **Instant cold start**: Native Android execution without JavaScript engine bootstrapping.
- **Pure Offline & Private**: No `INTERNET` permission required or requested.

## Features

- **Background Notification Interception**: Runs as an Android `NotificationListenerService` (`SmsMuterService`) to dismiss unwanted SMS notifications instantly.
- **Rule Matching Engine**:
  - Modes: `Contains`, `Exact`, `Starts with`, `Ends with`, `Wildcards` (`*`, `?`), and full `Regex`.
  - Targets: Match on sender (title), message text (body), or both.
  - Actions: Allow (show) or Block (mute). Allow rules always win.
  - Normalization: NFKC unicode-folding, zero-width character stripping, and loose separator stripping.
- **Dual Filtering Modes**:
  - **Only blocked** (default): Mutes only senders and words on your block list. Everything else notifies normally.
  - **All except allowed**: Strict allowlist mode — every SMS is muted unless it matches an allow rule.
- **Beginner-Friendly Home UI**: VPN-style master protection switch with plain-language status, plus 3 modern tabs (**Inbox**, **Shields**, **Rules**) for quick navigation.
- **Pattern Packs**: One-tap starter presets:
  - OTP & verification codes (seeded by default)
  - Trusted senders (e.g. CBE, seeded by default)
  - Scam & fraud words
  - Telecom / carrier promos
  - Suspicious link shorteners
- **Live "Try It" Tester**: Real-time rule preview against sample sender and text as you type.
- **Activity Log**: Chronological feed of seen SMS messages with relative timestamps, mute/shown status, and one-tap **Allow sender** / **Block sender** buttons.
- **Simulation Engine**: In-app test button to simulate incoming SMS messages for instant verification.

## Build and Run

### Requirements
- Android SDK (API 26+)
- JDK 17

### Gradle Commands

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Build optimized release APK (~628 KB)
./gradlew assembleRelease

# Build debug APK
./gradlew assembleDebug

# Install debug APK to connected device / emulator
./gradlew installDebug
```

The compiled release APK is located at:
`app/build/outputs/apk/release/app-release.apk`

### Automatic debug refresh while developing

Native Android does not have true Flutter/React-style hot reload. For the fastest
feedback loop, Android Studio can use **Run > Apply Changes** after a Kotlin or
layout edit. Changes to the manifest, dependencies, or app structure require a
normal restart.

On Windows, this project also includes a lightweight watcher that rebuilds,
installs, and relaunches the debug app whenever source files change:

```powershell
.\scripts\watch-debug.ps1
```

Requirements:

- A running emulator or a connected Android device
- `adb` available on `PATH`
- PowerShell execution of local scripts enabled

To build and install without relaunching the activity:

```powershell
.\scripts\watch-debug.ps1 -NoLaunch
```


The watcher only observes project source/configuration files and ignores
`app/build`, so generated build output cannot trigger an endless rebuild loop.

### Running on Emulator

1. **Start your Android Emulator** (e.g. `Small_Phone`):
   ```powershell
   emulator -avd Small_Phone
   ```
2. **Install and Launch the App**:
   ```powershell
   .\gradlew.bat installDebug
   adb shell am start -n com.example.ftele/.ui.MainActivity
   ```
3. **Grant Notification Listener Access** in the emulator:
   - Tap **Open notification access** warning on the home screen (or go to *Settings > Apps > Special app access > Device & app notifications*).
   - Toggle **F*ck Tele** to **Allow**.
4. **Test Interceptions**:
   - Tap **Simulate Real SMS** in the Activity tab or **Simulate Sample From Your Export** in the Shields tab to see live blocking and allowing in action!

## Architecture & Code Structure

- `app/src/main/java/com/example/ftele/`
  - `engine/Patterns.kt`: Core pattern-matching engine (wildcards, regex, Ethiopic Unicode normalization, decision logic).
  - `engine/PatternPacks.kt`: Preset behavioral categories and Shields (Financial & OTP Protection, Lottery, Telecom Promos, VAS, Loans, Pure Spam Senders).
  - `engine/Simulations.kt`: Authentic Ethiopian carrier & bank SMS samples for in-app testing.
  - `data/RuleStore.kt`: SharedPreferences store for rules, category states, settings, and activity logs.
  - `service/SmsMuterService.kt`: `NotificationListenerService` that intercepts and dismisses spam notifications in the background.
  - `ui/MainActivity.kt`: Ultra-lightweight native interface with Activity feed, Shields toggles, Custom Rules editor, and live Try-It preview.
  - `ui/ActivityAdapter.kt`: RecyclerView adapter for log entries.
  - `ui/RuleAdapter.kt`: RecyclerView adapter for custom rules.
- `app/src/test/java/com/example/ftele/PatternsTest.kt`: JUnit test suite verifying the matching engine.
