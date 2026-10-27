<div align="center">

# F-Tele

**High-Performance, Zero-Permission Native Android SMS Spam Muter**

[![Release](https://img.shields.io/badge/Release-v1.0.0-emerald?style=for-the-badge&logo=android)](https://github.com/Temesgendemeke/f-tele/releases)
[![APK Size](https://img.shields.io/badge/APK%20Size-~1.4%20MB-blue?style=for-the-badge)](https://github.com/Temesgendemeke/f-tele/releases)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20(API%2026%2B)-brightgreen?style=for-the-badge&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Privacy](https://img.shields.io/badge/Offline-100%25%20No%20Internet-red?style=for-the-badge&logo=shield)](https://github.com/Temesgendemeke/f-tele)
[![License](https://img.shields.io/badge/License-MIT-amber?style=for-the-badge)](LICENSE)

<p align="center">
  <b>Tired of non-stop telecom promotions, unsolicited lottery alerts, and marketing spam buzzing your phone all day?</b><br>
  F-Tele silences intrusive SMS notifications without deleting your messages. Everything stays safely in your normal inbox while your notification tray stays clean and quiet.
</p>

[Key Features](#key-features) •
[Why F-Tele?](#why-f-tele) •
[Screens & Architecture](#screens--ui-overview) •
[Installation](#installation) •
[Building](#building-from-source) •
[Privacy Guarantee](#privacy-guarantee)

---

</div>

## Key Features

- **Non-Destructive Notification Silencing**: Intercepts spam notifications at the Android system level (`NotificationListenerService`). Unwanted notifications vanish instantly; zero SMS messages are ever deleted or lost.
- **Behavioral Protection Shields**: Built-in category packs specifically tuned for regional telecom and marketing patterns (Ethio Telecom, Safaricom, telebirr, CBE, Awash, BOA, Dashen, loan apps, VAS subscriptions, and spam shortcodes).
- **Unconditional Allow-First Rule**: OTPs, bank transaction confirmations, airtime top-ups, and verification codes have absolute top priority and are never silenced.
- **Custom Rule Engine**: Create your own rules with `Contains`, `Exact`, `Starts With`, `Ends With`, `Wildcards (*, ?)`, and full regular expressions (`Regex`).
- **Dual Filtering Strategies**:
  - **Blocklist Mode (Default)**: Normal behavior — only senders and keywords matching your enabled shields or block rules are muted. Everything else notifies normally.
  - **Strict Allowlist Mode**: Extreme privacy mode — every incoming message is silenced unless it matches an explicit allow rule or verified financial shield.
- **Live In-App Rule Tester & SMS Simulator**: Test your keyword/regex patterns against sample text in real time, and simulate regional carrier and banking messages with a single tap.
- **Modern Material 3 Design**: Built 100% in Jetpack Compose with custom chiseled 3D shield graphics, subtle micro-interactions, full system dark/light theme support, and a high-contrast UI.

---

## Why F-Tele?

| Feature | F-Tele (Native Kotlin) | Typical SMS Blocker Apps | Default SMS Client |
| :--- | :---: | :---: | :---: |
| **Internet Permission** | None (0 network calls) | Yes (ad trackers, cloud sync) | Yes (RCS, telemetry) |
| **App Size** | ~1.4 MB | 30 MB – 80 MB | 50 MB+ |
| **Background RAM** | ~15 MB | 80 MB – 150 MB | System-managed |
| **Inbox Preservation** | 100% intact | Quarantines / deletes | Displays all spam |
| **Telemetry & Ads** | Zero | In-app banner / video ads | Marketing promotions |
| **Open Source** | Verifiable Codebase | Proprietary black-box | Proprietary |

---

## Screens & UI Overview

```
┌─────────────────────────────────────────────────────────────┐
│                       F-Tele Guard                          │
├─────────────┬─────────────┬─────────────┬───────────────────┤
│  Activity   │   Shields   │    Rules    │   Settings        │
└─────────────┴─────────────┴─────────────┴───────────────────┘
```

1. **Activity Feed**: Interactive master protection card featuring a dynamic 3D status crest, one-tap notification access permission status, live SMS activity stream, and quick-action **Allow sender** / **Block sender** buttons.
2. **Shields**: Behavioral protection presets:
   - **Financial & OTP Shield** (Always active): Protects bank credits/debits, 2FA, OTPs, and login codes.
   - **Telecom Promos & Packages**: Silences bulk package offers, internet discounts, and marketing campaigns.
   - **Lottery & Teleplay Contests**: Mutes prize draws, quizzes, and shortcode games.
   - **VAS & Third-Party Subscriptions**: Filters out ringtone clubs, horoscopes, and subscription popups.
   - **Unsolicited Loan Promos**: Blocks aggressive micro-lending and uncollateralized loan invitations.
   - **Pure Spam Shortcodes**: Known promotional bulk-sender numbers (`605`, `127` promo variants, etc.).
3. **Custom Rules**: Powerful filter builder with mode selection (Sender / Body / Both), pattern tester, and rule list management.
4. **Settings**: Strategy configuration (Blocklist vs Strict Allowlist), theme selector (System / Dark / Light), and notification listener diagnostics.

---

## Installation

### Method 1: Download Release APK (Recommended)

1. Grab the latest APK from the [Releases](https://github.com/Temesgendemeke/f-tele/releases) page (`app-release.apk` ~1.4 MB).
2. Install the APK on your Android device (Android 8.0+ / API 26+).
3. Open **F-Tele**, tap the attention banner, and toggle **F-Tele** ON under **Device & app notifications** (Notification Listener Access).

### Method 2: Install via ADB

```bash
# Install to connected device or emulator
adb install -r app/build/outputs/apk/release/app-release.apk

# Launch the main activity
adb shell am start -n com.qook.ftele/.ui.MainActivity
```

---

## Building From Source

### Prerequisites
- **JDK 17** or higher
- **Android SDK** (API 26+)
- Gradle (use the bundled `./gradlew` wrapper)

### Build Commands

```bash
# Clone the repository
git clone https://github.com/Temesgendemeke/f-tele.git
cd f-tele

# Run unit test suite
./gradlew testDebugUnitTest

# Build optimized minified Release APK (~1.4 MB)
./gradlew assembleRelease

# Build Debug APK
./gradlew assembleDebug

# Run Android Lint checks
./gradlew lint
```

The compiled release APK will be located at:
```
app/build/outputs/apk/release/app-release.apk
```

---

## Privacy Guarantee

F-Tele was built from day one as a verifiable offline privacy tool:

1. **No Network Capability**: The application does **not** declare `android.permission.INTERNET` in its manifest. On Android, an application without this permission is physically incapable of opening sockets or transmitting data off your device.
2. **Local Storage Only**: Rules, settings, and seen notification summaries are kept strictly within your device's private `SharedPreferences` database.
3. **No Third-Party SDKs**: No analytics libraries, no crash trackers, no advertising frameworks, and no external binary dependencies.

---

## Project Architecture

```
app/src/main/
├── AndroidManifest.xml
└── java/com/qook/ftele/
    ├── data/
    │   └── RuleStore.kt          # SharedPreferences persistence for rules, logs, and settings
    ├── engine/
    │   ├── Patterns.kt           # Normalized matching engine (Exact, Prefix, Wildcard, Regex)
    │   ├── PatternPacks.kt       # Behavioral shields and starter pattern rules
    │   └── Simulations.kt        # Regional carrier and banking SMS test cases
    ├── model/
    │   └── Models.kt             # Data classes (Rule, LogEntry, Decision, Category)
    │── service/
    │   └── SmsMuterService.kt    # Android NotificationListenerService for intercepting SMS
    └── ui/
        ├── MainActivity.kt       # Single-activity host with reactive state management
        ├── MainScreen.kt         # Scaffold, navigation bars, and top app bar
        ├── components/           # Custom 3D Shield canvas & bottom navigation bar
        ├── screens/              # Activity, Shields, Rules, and Settings tabs
        └── theme/                # Material 3 colors, typography, and styling tokens
```

---

## Contributing

Contributions are very welcome! If you want to add new regional carrier pattern presets, improve matching performance, or report a bug:

1. Fork the repository.
2. Create your feature branch (`git checkout -b feat/new-pattern-pack`).
3. Commit your changes (`git commit -m 'feat: add pattern shield for new carrier'`).
4. Ensure all unit tests pass (`./gradlew testDebugUnitTest`).
5. Open a Pull Request.

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.
