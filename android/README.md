# Reflex7 for Android

This directory contains the native Android port of Reflex7. It is implemented in Kotlin and Jetpack Compose; it does not use a WebView, Trusted Web Activity, embedded website, backend, or network permission.

## Architecture

- `engine/` contains the 34-entry task registry, Task Engine v2 selection/scoring logic, seedable randomness, monotonic timer, data models, and task factory.
- `GameViewModel.kt` owns session state, task transitions, input isolation, pause/resume, scoring, records, cleanup, and lifecycle-safe delayed events.
- `MainActivity.kt` renders the menu, game, pause, results, onboarding, and how-to UI with Compose.
- `data/PreferencesRepository.kt` persists local preferences, discoveries, and separate 7-second/4-second records with DataStore Preferences.
- `audio/RetroAudio.kt` generates short retro cues with Android `ToneGenerator`.
- `values/` and `values-tr/` contain matching English and Turkish resources.

The engine is UI-independent. Compose receives immutable `GameUiState` and sends explicit input actions to the view model. Each task round has a generation identifier, so callbacks and releases from a previous round cannot mutate the next round.

## Setup and run

1. Install Android Studio with Android SDK 36 and JDK 17 or newer.
2. Open the `android` directory as a project.
3. Let Gradle sync, then run the `app` configuration on an Android 7.0 (API 24) or newer device/emulator.

From PowerShell:

```powershell
cd android
.\gradlew.bat installDebug
```

## Test and validate

```powershell
.\gradlew.bat test
.\gradlew.bat lint
.\gradlew.bat assembleDebug
```

The unit suite covers registry integrity, Rhythm removal, selection repetition and penalties, eligibility, compatibility, deterministic selection, scoring/combo behavior, timer pause/resume, mode-specific record models, task construction, and delayed-signal feasibility.

## Feature parity

- All 34 currently registered web tasks are represented; the intentionally removed Rhythm task is absent.
- Task Engine v2 preserves weighted selection, immediate-repeat prevention, history/category penalties, level/duration/memory requirements, difficulty bands, controlled random selection, six modifiers, and five global rules.
- The exact web scoring formula, combos, highest combo, mode-specific local records, nickname, sound setting, discoveries, and onboarding state are preserved.
- The native screens provide both game modes, TR/EN switching, audio, pause/resume, retry, results, local records, and how-to content.
- Touch, hardware keyboard activation, hold/release, repeated taps, ordered choices, lifecycle pausing, and a monotonic clock are supported.

## Known differences and limitations

- The native UI recreates the web game's dark terminal/CRT identity in Compose, but pixel measurements and Android font rendering are platform-native rather than browser-identical.
- Retro cues are generated with `ToneGenerator`; their timbre can vary slightly across devices.
- Records and preferences are device-local. There is no account, cloud sync, or online leaderboard.
- Automated tests validate engine behavior; final touch ergonomics, audio volume, accessibility services, and unusual OEM lifecycle behavior should also be checked on physical phones and tablets.
- The web project remains independent at the repository root and retains its existing deployment workflow.

## APK

After a debug build, the APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
