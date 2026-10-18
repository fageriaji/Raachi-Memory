# Raachi Memory v1.0.3 Architecture

## 1. Architectural Goals

- Offline-first operation with no mandatory account.
- Reliable reminder and ledger alarms.
- Clear ownership boundaries between UI, domain, and data code.
- User-data preservation across migrations, import, and application updates.
- Minimal Android permissions and system-picker-first access to personal data.
- Extensibility for encrypted cloud backup without replacing local storage.

## 2. Technology Stack

- Kotlin
- Java 17 target
- Jetpack Compose and Material 3
- Navigation Compose
- Hilt
- Room
- Proto DataStore
- Coroutines, Flow, and StateFlow
- AlarmManager
- BroadcastReceiver
- WorkManager dependency for future persistent background work
- AndroidX Biometric
- Android Photo Picker and Image Cropper

Platform configuration:

- Minimum SDK: 26
- Compile SDK: 36
- Target SDK: 36
- Application ID: `com.raachi.memory`

## 3. Application Layers

The practical dependency flow is:

```text
Compose screen
    -> ViewModel
    -> domain use case / manager
    -> repository interface
    -> Room DAO, Proto DataStore, AlarmManager, or Android service
```

Not every simple read requires a dedicated use case, but Composables never access Room or DataStore directly.

### UI Layer

- Compose screens render immutable UI state.
- ViewModels expose `StateFlow` and accept user events.
- Navigation callbacks are passed into screens.
- Android activity-result contracts handle photo, contact, file, and permission flows.

### Domain Layer

- Domain models use typed enums and Java time types.
- Use cases perform validation, persistence orchestration, activity logging, and alarm scheduling.
- Repository interfaces isolate platform and storage implementations.
- App Lock hashing and session state are owned by `AppLockManager`.

### Data Layer

- Room is the source of truth for profile, reminders, ledger, activity, and expenses.
- Proto DataStore is the source of truth for preferences and App Lock credential hashes.
- Repositories map between Room entities and domain models.
- `BackupManager` serializes and validates versioned JSON backups.
- Alarm schedulers isolate `AlarmManager` and `PendingIntent` behavior.

## 4. Source Organization

```text
app/src/main/java/com/raachi/memory/
|-- core/
|   |-- navigation/
|   |-- ui/
|   `-- util/
|-- data/
|   |-- activity/
|   |-- backup/
|   |-- database/
|   |-- expense/
|   |-- ledger/
|   |-- profile/
|   |-- reminder/
|   `-- settings/
|-- di/
|-- domain/
|   |-- model/
|   |-- repository/
|   |-- security/
|   `-- usecase/
|-- feature/
|   |-- activity/
|   |-- dashboard/
|   |-- expense/
|   |-- ledger/
|   |-- onboarding/
|   |-- profile/
|   |-- reminder/
|   |-- security/
|   `-- settings/
|-- MainActivity.kt
`-- RaachiMemoryApplication.kt
```

## 5. Navigation Architecture

`RaachiMemoryApp` owns a single Navigation Compose host and modal drawer.

Start flow:

```text
Splash/Foundation -> Welcome -> Onboarding Profile -> Dashboard
```

Returning users start at Dashboard. Primary destinations are Home, Reminders, Ledger, Expenses, Activity, Profile, Backup & Restore, Settings, and About. Editor routes use optional identifiers for create/edit behavior.

The drawer is available on primary destinations. Bottom navigation contains Home, Reminders, Ledger, and Expenses. Editors, App Lock settings, and profile editing use normal back-stack navigation.

## 6. Persistence Architecture

### Room

Room database version 7 contains:

1. `user_profile`
2. `reminders`
3. `ledger_entries`
4. `activity_logs`
5. `expense_accounts`
6. `expense_transactions`

The complete schema and migration rules are defined in `DATABASE.md` and the exported Room schemas under `app/schemas`.

### Proto DataStore

DataStore contains:

- Onboarding completion
- Theme mode
- Reminder sound preference
- Default snooze duration
- Quiet Hours enabled state, start time, and end time
- App Lock enabled state
- Salted passcode and recovery-code hashes
- Biometric preference

### App-Private Files

The cropped profile photo is copied to app-private storage. Room stores its app-controlled URI. The profile photo is embedded in backup schema 3 so it can be restored independently of the original Photo Picker URI.

## 7. Reminder and Ledger Scheduling

Reminder and ledger schedulers use exact, allow-while-idle alarms when exact-alarm access is available. Notification permission and exact-alarm access are surfaced in Settings.

Reminder notification actions:

- Snooze
- Done
- Skip

Ledger notification actions:

- Mark Returned
- Open WhatsApp when a suitable mobile number is present

The app uses a stable named Android resource URI for `raachisound.mp3`. Versioned notification-channel IDs replace older channels when the bundled sound configuration changes.

Interval reminders pass through a shared Quiet Hours policy before scheduling and again when an alarm is received. Quiet Hours are enabled by default from 23:00 to 06:00, can be customized in Reminder Preferences, and can be bypassed per interval reminder. Suppressed occurrences do not post a notification or activity event. One-time, daily, and weekly recurrence is unchanged.

Changing Quiet Hours re-anchors and persists active interval schedules so Dashboard and Reminder screens show the next deliverable occurrence. The receiver-side check protects against stale alarms created before a preference change.

`ReminderBootReceiver` reconciles reminder and ledger alarms after:

- Device boot
- Application package replacement
- Manual device-time change
- Timezone change

## 8. Backup and Restore

Current backup schema: 4. Supported import schemas: 1 through 4.

The current local backup is timestamped JSON containing profile, profile photo, reminders, ledger entries, activity logs, expense accounts, expense transactions, and normal preferences. Imports are size-limited, schema-validated, enum-validated, and relation-validated before destructive changes begin.

Database replacement runs in a Room transaction. Existing alarms are cancelled and active reminder/ledger alarms are reconciled after a successful import, including Quiet Hours. App Lock credentials are not part of the exported preferences.

Encrypted Google Drive backup is intentionally deferred. Its future implementation must wrap the existing validated snapshot rather than introduce a second data model. Room must remain the source of truth.

## 9. App Lock Architecture

- Custom passcode: exactly six digits.
- Recovery code: 12 random digits displayed in groups of four.
- Hashing: PBKDF2-HMAC-SHA256, per-value 16-byte salt, 210,000 iterations, 256-bit output.
- Stored values: hashes and salts only.
- Optional biometric authentication through AndroidX Biometric.
- Optional reset through secure device credentials.
- In-memory unlocked session; the session locks when the app leaves the foreground.

System photo picking and cropping use a narrowly scoped trusted-media flow so returning from the system UI does not lose the user's navigation state. Backgrounding the crop UI still locks the application.

## 10. Privacy and Permission Boundaries

Manifest permissions are limited to notifications, exact alarms, and boot completion. The app does not currently declare internet, contacts, SMS, storage, location, microphone, or camera permissions.

- Contacts are selected through the Android contact picker.
- Photos are selected through Android Photo Picker.
- App-private profile images are shared with the crop activity through a non-exported `FileProvider` grant.
- Android automatic backup is disabled.

Any future ads, cloud backup, sync, or SMS transaction detection must update these boundaries, the privacy policy, and Play declarations.

## 11. Failure Handling

- Invalid editor input remains in the UI with field-level errors.
- Missing notification/exact-alarm access is shown with a route to system settings.
- Backup imports fail before replacement when validation fails.
- Expense transfers enforce distinct source and destination accounts.
- Account deletion is restricted while referenced by transactions.
- Alarm receivers ignore missing or inactive records.

## 12. Testing Boundaries

- Unit tests cover validation, recurrence, calculations, security, mapping, and backup-related behavior.
- Instrumentation tests cover Room persistence/migrations and selected Compose UI flows.
- Physical-device testing remains mandatory for alarms, notification channels, custom sound, biometrics, Doze, reboot, and manufacturer battery controls.

## 13. Architectural Decisions

- ADR-001: Kotlin and Jetpack Compose for the Android application.
- ADR-002: Room is the only application record source of truth.
- ADR-003: Proto DataStore stores preferences and App Lock hashes.
- ADR-004: AlarmManager owns exact user-facing reminders; WorkManager is not used as an exact alarm.
- ADR-005: System pickers are preferred over broad personal-data permissions.
- ADR-006: Manual backups are versioned, validated, and backward compatible.
- ADR-007: Cloud capability must remain optional and preserve offline-first behavior.
