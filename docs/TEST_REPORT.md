# Raachi Memory Release Candidate Test Report

Date: 1 August 2026

## Environment

- Android 16/17 preview Pixel emulator
- JBR 21.0.11
- Debug APK tested with an isolated copy of existing offline user data
- Release APK built with R8 minification and resource shrinking

## Automated Results

- Unit tests: 43 passed, 0 failed
- Room/DAO instrumentation tests: 2 passed, 0 failed
- Compose instrumentation tests: 3 blocked by an Android 17 preview Espresso `InputManager.getInstance()` incompatibility
- Android lint: 0 errors, 40 advisory warnings
- Debug build: passed
- Minified release build: passed

## Covered Areas

- Profile validation, onboarding, and BMI calculation
- Reminder validation, repeat calculations, snooze defaults, and daylight-saving behavior
- Ledger validation, currency conversion, sharing text, due alerts, and overdue scheduling
- Expense account validation, debit/credit/transfer balance calculations, and insufficient-funds protection
- Activity mapping, filtering, and search
- Room DAO persistence, foreign keys, filtering, deletion, and ordering
- Full Room migration chain from version 1 to version 6 with profile preservation
- About screen rendering and light/dark theme checks (previously passing; blocked only on the Android 17 preview runner in this pass)

## Manual Emulator Checks

- Existing Room v5 data copied into an isolated package and migrated to v6 without data loss
- Dashboard Daily Expenses card and empty Expense Overview rendering
- Add-account bottom sheet rendering after correcting packed-color conversion
- App launch after migration with no crash in logcat
- Reminder notification delivery and notification actions
- Ledger due notification, Mark Returned action, and next-day rescheduling
- WhatsApp launch/fallback workflow
- Privacy-preserving Android contact picker launch from the Ledger editor
- Adaptive launcher icon safe-zone rendering with the circular launcher mask
- Live System, Light, and Dark theme switching
- Timestamped JSON export and validated JSON import
- Alarm rescheduling after import

## Remaining Physical-Device Checks

- Add/edit several bank, cash, and wallet accounts
- Debit, credit, transfer, filtering, deletion, and balance recalculation
- Reboot, Doze, battery saver, timezone changes, and manufacturer battery restrictions
- Large fonts, TalkBack, contrast, and small-screen layouts

## Release Candidate State

- Room schema: version 6
- Backup schema: version 2 with backward-compatible version 1 import
- Release APK: unsigned; signing remains part of Phase 11
