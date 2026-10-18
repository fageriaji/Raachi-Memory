# Raachi Memory v1.0.3

## Master Product Requirements Document

**Tagline:** Never forget what matters.

## 1. Product Summary

Raachi Memory is a privacy-first, offline Android application for personal reminders, informal lending records, and daily expense tracking. It does not require an account or an internet connection for any current feature.

The application helps users:

- Schedule reliable personal reminders.
- Track money and items lent or borrowed.
- Maintain balances across bank, cash, and wallet accounts.
- Review reminder and ledger activity.
- Protect the application with a passcode and optional biometrics.
- Export and restore a local, versioned backup.

## 2. Product Promise

Raachi Memory must remain useful without connectivity and must not silently send personal, financial, reminder, contact, or profile data off the device.

> Never forget what matters.

## 3. Current Release

- Version name: `1.0.3`
- Version code: `3`
- Minimum Android version: Android 8.0, API 26
- Compile SDK: 36
- Target SDK: 36
- Distribution state: signed-device testing and release preparation

## 4. Target Users

- People managing recurring routines such as water, medicine, meals, exercise, or sleep.
- People who informally lend or borrow money and belongings.
- People who want a lightweight daily expense manager without connecting bank credentials.
- Users who prefer local storage and optional manual backups.

## 5. Implemented Features

### 5.1 Onboarding and Profile

- Branded splash and welcome flow.
- First-run profile collection with required name.
- Optional date of birth, mobile number, gender, email, height, and weight.
- Native date picker; date of birth stored in `DD-MM-YYYY` format.
- Indian mobile-number validation: exactly 10 digits, starting with 5, 6, 7, 8, or 9.
- Supported-provider email validation.
- Gender options: Male, Female, and Other.
- BMI calculated from height and weight rather than stored.
- Profile photo selection through Android Photo Picker.
- User-controlled square image crop and app-private photo storage.
- Editable profile with photo removal.

### 5.2 Dashboard

- Centered Raachi Memory title and profile shortcut.
- Time-sensitive greeting and greeting icon.
- Current weekday and date.
- Summary cards for reminders, ledger, and completed reminders.
- Upcoming reminder cards with category-specific full-color emoji.
- Pending ledger cards with money/item visual treatment.
- Quick-add actions for reminders, ledger entries, and expenses.

### 5.3 Reminders

- Create, edit, and delete reminders.
- Categories: Water, Medicine, Breakfast, Lunch, Dinner, Exercise, Sleep, and Custom.
- Repeat rules: one-time, daily, weekly, and every X hours.
- Native date and time selection.
- New reminders must be scheduled in the future.
- Active, Snoozed, and Completed list tabs.
- Snooze, Done, and Skip actions.
- Exact alarms through `AlarmManager` when permission is available.
- Notification actions for Snooze, Done, and Skip.
- Bundled Raachi notification sound and optional vibration.
- Configurable Quiet Hours for interval reminders, enabled by default from 11:00 PM to 6:00 AM.
- Per-interval override to allow a specific reminder during Quiet Hours.
- Quiet Hours defer interval delivery without creating a false skipped or completed activity event; one-time, daily, and weekly reminders remain unaffected.
- Alarm restoration after reboot, app update, manual time change, and timezone change.

### 5.4 Ledger

- Track money or an item.
- Track Lent or Borrowed direction.
- Record transaction date and optional expected return date.
- Store person name, optional mobile number, notes, amount, or item description.
- Create, edit, delete, and mark returned.
- All, Lent, and Borrowed tabs.
- Due and overdue alerts.
- Notification action to mark an entry returned.
- WhatsApp launch for the stored number when possible.
- WhatsApp Business and Android share-sheet fallback.
- Native Android contact picker fills both contact name and phone number without broad contacts permission.

### 5.5 Daily Expense Manager

- Multiple Bank, Cash, and Wallet accounts.
- Opening balance for each account.
- Debit, Credit, and Transfer transactions.
- Transfers move money between accounts without changing the combined balance.
- Categories for common spending and income types.
- Payment methods: UPI, debit card, cash, net banking, and other.
- Today, This Week, This Month, and All date filters.
- Debit, Credit, Transfer, and All type filters.
- Add, edit, and delete transactions.
- Insufficient-funds and account-selection validation.
- All amounts stored as integer paise.
- No bank credentials, SMS access, or direct bank integration.

### 5.6 Activity

- Chronological reminder and ledger event timeline.
- Reminder and ledger filters.
- Search by title and description.
- Events for creation, update, alerts, snooze, completion, skip, return, and deletion.

Expense transactions currently remain inside Expense Manager and are not written to the Activity timeline.

### 5.7 Navigation

- Modal navigation drawer on primary screens.
- Drawer header with a 100dp circular profile image and user name.
- Drawer destinations: Home, Reminders, Ledger, Expenses, Activity, Profile, Backup & Restore, Settings, and About.
- Compact bottom navigation for Home, Reminders, Ledger, and Expenses.
- Hamburger menu on primary screens and back navigation on editor/detail flows.

### 5.8 App Lock

- Optional custom 6-digit app passcode.
- Optional biometric unlock using device-supported biometrics.
- Securely generated 12-digit recovery code displayed in readable groups.
- Device PIN, pattern, or password reset path when a secure device credential exists.
- Salted PBKDF2-HMAC-SHA256 passcode and recovery-code hashes.
- Lock on backgrounding, with protected handling for system photo selection and cropping.
- No default or universal recovery password.

### 5.9 Settings, Backup, and About

- System, Light, and Dark themes.
- Reminder sound enable/disable.
- Default snooze choices: 5, 10, 15, 30, or 60 minutes.
- Reminder Preferences with an enabled-by-default, user-configurable Quiet Hours schedule.
- Notification and exact-alarm permission controls.
- Timestamped JSON export.
- Validated JSON import with transactional replacement and alarm rescheduling.
- Backup schemas 1 through 4 supported for import; current export schema is 4.
- About screen with branding, version, privacy message, and technology summary.

## 6. Privacy and Permissions

Current manifest permissions are limited to:

- `POST_NOTIFICATIONS`
- `SCHEDULE_EXACT_ALARM`
- `RECEIVE_BOOT_COMPLETED`

Profile photos and contacts use Android system pickers. The app does not request broad photo-library, contacts, SMS, location, microphone, or internet permissions in v1.0.3. Android automatic application backup is disabled; users control local export and import.

## 7. Design System

- Jetpack Compose and Material 3.
- Bundled Nunito font family.
- Brand navy/indigo and orange derived from the Raachi Memory logo.
- Purple ledger identity, coral money identity, teal item identity, and green completion states.
- Category-specific full-color emoji for reminder recognition.
- Responsive light and dark color schemes without brown-dominant surfaces.
- Adaptive, round, themed, splash, in-app, and density-specific launcher assets.

See `UI_GUIDELINES.md` for implementation guidance.

## 8. Technical Foundation

- Kotlin and Java 17 bytecode target.
- Jetpack Compose with Material 3.
- MVVM with repository and use-case layers.
- Room 7 database schema as the local source of truth.
- Proto DataStore for preferences and App Lock credentials.
- Hilt dependency injection.
- Coroutines, Flow, and StateFlow.
- AlarmManager and broadcast receivers for reminder and ledger alarms.
- WorkManager dependency retained for future scheduled backup/reconciliation work.

## 9. Deferred Features

The following are not implemented in v1.0.3:

- Encrypted Google Drive backup and automatic weekly rotation.
- Google account login.
- Live multi-device synchronization.
- AdMob or other advertising.
- SMS-based automatic expense detection.
- Budgets, spending charts, and expense export.
- Widgets, voice input, shared records, and AI capture.

## 10. Release Definition of Done

The application is ready for production when:

- Current automated tests, lint, debug, and minified release builds pass.
- Reminder and ledger alarms survive reboot and relevant time changes on physical devices.
- Notification actions and custom sound work on representative Android devices.
- Backup export/import is regression-tested with current and older supported schemas.
- Small screens, large fonts, TalkBack, contrast, and touch targets pass review.
- Store listing, privacy policy, Data Safety, exact-alarm declaration, and signed AAB are complete.
- Play Console internal testing and pre-launch reports contain no blocking issue.

## 11. Success Measures

- Reminders fire reliably and recover after device events.
- Users can create a reminder or record within a few clear steps.
- Financial balances remain mathematically correct.
- Users can restore their data without corruption.
- The app remains stable, understandable, and trustworthy.
