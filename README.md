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

Run inside the `android/` directory:

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Build optimized release APK (2.2 MB)
./gradlew assembleRelease

# Build debug APK
./gradlew assembleDebug

# Install debug APK to connected device / emulator
./gradlew installDebug
```

The compiled release APK is located at:
`android/app/build/outputs/apk/release/app-release.apk`

## Architecture & Code Structure

- `android/app/src/main/java/com/example/ftele/`
  - `engine/Patterns.kt`: Core pattern-matching engine (wildcards, regex, normalization, decision logic).
  - `engine/PatternPacks.kt`: Preset pattern packs and default seeding.
  - `engine/Simulations.kt`: Sample messages for in-app testing.
  - `data/RuleStore.kt`: SharedPreferences store for rules, settings, and activity logs.
  - `service/SmsMuterService.kt`: `NotificationListenerService` that catches and dismisses notifications in the background.
  - `ui/MainActivity.kt`: Material 3 user interface with live Try-It evaluation, Activity feed, and Rule editor.
  - `ui/ActivityAdapter.kt`: RecyclerView adapter for log entries.
  - `ui/RuleAdapter.kt`: RecyclerView adapter for custom rules.
- `android/app/src/test/java/com/example/ftele/PatternsTest.kt`: JUnit test suite verifying the matching engine.
