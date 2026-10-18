# Raachi Memory v1.0.3 Test Report

**Updated:** 11 August 2026

**Application version:** `1.0.3` (`versionCode 3`)

**Room schema:** 7

**Backup schema:** 4; imports supported from schemas 1 through 4

## 1. Build Environment

- JBR 21.0.11
- Java 17 bytecode target
- Gradle 8.13
- Android Gradle Plugin 8.11.2
- Compile SDK 36
- Target SDK 36
- Minimum SDK 26
- R8 minification and resource shrinking enabled for release

## 2. Latest Automated Verification

Command scope:

```text
:app:testDebugUnitTest
:app:compileDebugAndroidTestKotlin
:app:lintDebug
:app:assembleDebug
:app:assembleRelease
```

Results:

- Unit tests: 56 passed, 0 failed, 0 skipped across 13 suites.
- Android instrumentation-test sources: compiled successfully.
- Android lint: 0 errors, 46 advisory warnings.
- Debug APK build: passed.
- Minified release APK build: passed.
- Full Gradle verification completed successfully on 11 August 2026.

The 46 lint warnings are non-blocking advisory findings. They must still be reviewed when dependencies, target SDK, accessibility behavior, or release policy changes.

## 3. Unit-Test Coverage

Automated unit coverage includes:

- Profile validation, date formatting, email/mobile rules, and BMI calculation
- Reminder validation, recurrence, snooze, completion, skip, daylight-saving behavior, and Quiet Hours boundaries/cadence
- Ledger validation, paise conversion, WhatsApp/share text, due alerts, and scheduling
- Expense account and transaction validation
- Debit, credit, transfer, combined-balance, and insufficient-funds calculations
- Activity mapping, filtering, and search
- App Lock passcode/recovery hashing and state transitions
- Dashboard date formatting, greeting boundaries, and category visuals
- Profile date-of-birth persistence mapping

## 4. Instrumentation Coverage

The project currently contains five Android instrumentation test methods:

- Two Room/DAO and migration-oriented tests passed in the last compatible emulator run.
- Three Compose instrumentation tests were blocked on the Android 17 preview emulator by an Espresso `InputManager.getInstance()` incompatibility rather than an application assertion failure.
- Instrumentation test sources compile against the current application APIs. Device tests were not executed during this feature update.

Instrumentation coverage includes Room persistence, foreign keys, filtering, deletion, ordering, migration, About rendering, and theme behavior.

## 5. Verified Manual Flows

The following flows have been exercised during v1.0.3 development on the emulator and/or a physical Samsung device:

- First-run onboarding and validated profile editing
- Native profile photo selection, crop, change, removal, and app-private persistence
- Profile photo flow while App Lock is enabled
- App Lock passcode, recovery code, device-credential path, and optional biometric behavior
- Drawer and bottom-navigation routing from Dashboard and Profile
- Reminder create/edit/delete with future date and time
- Reminder delivery and Snooze, Done, and Skip notification actions
- Stable custom-sound notification channel and runtime resource-URI playback
- Reminder recovery after reboot/package/time/timezone receiver events in development testing
- Ledger create/edit/delete/return workflows
- Contact picker filling contact name and mobile number
- WhatsApp, WhatsApp Business, and Android share-sheet fallback
- Ledger due alert and Mark Returned notification action
- Expense account creation for bank, cash, and wallet
- Debit, credit, transfer, filter, edit, delete, and balance recalculation
- System, Light, and Dark theme switching and system-bar icon appearance
- Adaptive/round launcher mask and splash/logo rendering
- Timestamped JSON export, validated import, and alarm rebuilding
- Room migration chain through version 7 without destructive migration

## 6. Remaining Release Checks

Before production publication, complete a structured physical-device matrix covering:

- Clean install and upgrade from tagged `v1.0.1` and `v1.0.3` candidates
- Reboot and full shutdown/startup
- Doze, battery saver, and long idle periods
- Manual clock and timezone changes
- Android 8 through current supported Android releases where practical
- Samsung and other manufacturer battery restrictions
- Notification permission denied/re-enabled
- Exact-alarm permission denied/re-enabled
- Default and customized Quiet Hours, daytime/cross-midnight ranges, interval override, and rescheduling after a preference change
- Quiet Hours behavior across reboot, Doze, battery saver, manual clock changes, and timezone changes
- Notification channel behavior after upgrading from older channel IDs
- Custom sound with silent, vibrate, DND, Bluetooth, and media-routing configurations
- Biometric enrollment changes and unavailable biometric hardware
- Local backup imports for schemas 1, 2, 3, and 4
- Corrupt, oversized, tampered, and wrong-format backup files
- Small screens, display cutouts, landscape interruption, and process recreation
- 1.3x to 2.0x font scales
- TalkBack labels, focus order, contrast, and 48dp touch targets
- Minified signed APK/AAB installed from the intended release signing key

## 7. Release Assessment

The v1.0.3 source passes its current automated unit, lint, debug-build, and minified-release-build gates. Core offline features have been exercised successfully during iterative emulator and physical-device testing.

Production readiness still depends on completing the remaining physical-device/accessibility matrix and Play Store release work. Encrypted Google Drive backup is a separate future phase and is not part of the v1.0.3 release assessment.
