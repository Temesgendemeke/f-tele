This is a pure native Kotlin Android mobile application. Prioritize performance, ultra-low APK size, battery efficiency, and offline-first privacy.

## Project Structure

This is a standard modern Android Gradle project:

```
├── app/
│   ├── src/main/java/com/example/ftele/
│   │   ├── data/       # Persistence (RuleStore, SharedPreferences)
│   │   ├── engine/     # Core pattern matching, packs, and simulations
│   │   ├── model/      # Data models (Rule, LogEntry, Decision)
│   │   ├── service/    # SmsMuterService (NotificationListenerService)
│   │   └── ui/         # MainActivity, MainScreen, and Jetpack Compose screens
│   ├── src/main/res/   # Drawables, styles, and values
│   ├── src/test/       # Unit tests (JUnit 4)
│   └── build.gradle    # Module build configuration
├── build.gradle        # Top-level Gradle configuration
├── settings.gradle     # Project settings
└── gradle.properties   # Project build properties
```

## Commands

Use Gradle wrapper (`./gradlew` or `.\gradlew.bat` on Windows):

```bash
./gradlew testDebugUnitTest  # run unit tests
./gradlew assembleDebug      # build debug APK
./gradlew assembleRelease    # build optimized release APK (R8 minified)
./gradlew lint               # run Android lint checks
```

## Architecture & Conventions

- **100% Offline**: Never add network permissions (`android.permission.INTERNET`). The app is designed as a verifiable offline privacy tool.
- **Low Memory & Performance**: Avoid heavy third-party libraries. Use Android platform APIs, native Jetpack Compose, and lightweight native components.
- **Service Stability**: `SmsMuterService` runs in the background. Keep notification processing asynchronous, non-blocking, and free of memory leaks.
