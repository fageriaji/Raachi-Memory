# Raachi Memory v1.0.3 Data Design

## 1. Storage Overview

- Room database version: `7`
- Backup export schema: `4`
- Supported backup imports: schemas `1` through `4`
- Room is the source of truth for application records.
- Proto DataStore is the source of truth for preferences and App Lock credentials.
- Profile photos are stored in app-private files and referenced by Room.
- Monetary values are stored as integer paise.

## 2. Room Tables

### `user_profile`

Stores the single local user profile.

| Column | Type | Required | Notes |
|---|---|---:|---|
| `id` | Long | Yes | Primary key; current profile uses ID 1 |
| `name` | String | Yes | Only mandatory onboarding field |
| `dateOfBirth` | String | No | `DD-MM-YYYY` |
| `mobile` | String | No | Validated 10-digit Indian mobile number |
| `gender` | String | No | `MALE`, `FEMALE`, or `OTHER` |
| `email` | String | No | Validated supported-provider email |
| `heightCm` | Double | No | Used for BMI calculation |
| `weightKg` | Double | No | Used for BMI calculation |
| `profilePhotoUri` | String | No | App-private persisted photo URI |
| `createdAtMillis` | Long | Yes | Unix epoch milliseconds |
| `updatedAtMillis` | Long | Yes | Unix epoch milliseconds |

BMI and age are derived values and are not stored.

### `reminders`

Stores scheduled reminder definitions and their current state.

| Column | Type | Required | Notes |
|---|---|---:|---|
| `id` | Long | Yes | Auto-generated primary key |
| `title` | String | Yes | Reminder title |
| `category` | String | Yes | Typed category enum name |
| `description` | String | No | Optional details |
| `repeatType` | String | Yes | `ONE_TIME`, `DAILY`, `WEEKLY`, or `INTERVAL` |
| `intervalHours` | Int | No | Required for interval reminders |
| `scheduledAtMillis` | Long | Yes | Original selected schedule |
| `nextTriggerAtMillis` | Long | No | Next alarm instant; null when no future occurrence exists |
| `soundEnabled` | Boolean | Yes | Per-reminder sound state |
| `vibrationEnabled` | Boolean | Yes | Per-reminder vibration state |
| `allowDuringQuietHours` | Boolean | Yes | Interval-only override; defaults to false |
| `status` | String | Yes | `ACTIVE`, `SNOOZED`, `COMPLETED`, `SKIPPED`, or `ARCHIVED` |
| `createdAtMillis` | Long | Yes | Unix epoch milliseconds |
| `updatedAtMillis` | Long | Yes | Unix epoch milliseconds |

Indexes:

- `nextTriggerAtMillis`
- `status`

Categories:

- `WATER`
- `MEDICINE`
- `BREAKFAST`
- `LUNCH`
- `DINNER`
- `EXERCISE`
- `SLEEP`
- `CUSTOM`

### `ledger_entries`

Stores money and item records for lending and borrowing.

| Column | Type | Required | Notes |
|---|---|---:|---|
| `id` | Long | Yes | Auto-generated primary key |
| `personName` | String | Yes | Person associated with the entry |
| `mobileNumber` | String | No | Used for WhatsApp/reminder sharing |
| `kind` | String | Yes | `MONEY` or `ITEM` |
| `direction` | String | Yes | `LENT` or `BORROWED` |
| `itemName` | String | No | Required by item entries |
| `amountPaise` | Long | No | Required by money entries |
| `transactionDateEpochDay` | Long | Yes | Lent/borrowed date |
| `dueDateEpochDay` | Long | No | Expected return date |
| `isReturned` | Boolean | Yes | Completion state |
| `returnedAtMillis` | Long | No | Return instant |
| `notes` | String | No | Optional notes |
| `createdAtMillis` | Long | Yes | Unix epoch milliseconds |
| `updatedAtMillis` | Long | Yes | Unix epoch milliseconds |

Indexes:

- `dueDateEpochDay`
- `isReturned`

### `activity_logs`

Stores immutable reminder and ledger history entries.

| Column | Type | Required | Notes |
|---|---|---:|---|
| `id` | Long | Yes | Auto-generated primary key |
| `eventType` | String | Yes | Typed activity event name |
| `referenceId` | Long | No | Related reminder or ledger ID |
| `title` | String | Yes | User-facing event title |
| `description` | String | No | Optional event details |
| `eventTimeMillis` | Long | Yes | Unix epoch milliseconds |

Indexes:

- `eventTimeMillis`
- `eventType`

Tracked reminder events: created, updated, alert sent, snoozed, completed, skipped, and deleted.

Tracked ledger events: created, updated, alert sent, returned, and deleted.

### `expense_accounts`

Stores balance sources.

| Column | Type | Required | Notes |
|---|---|---:|---|
| `id` | Long | Yes | Auto-generated primary key |
| `name` | String | Yes | User-defined account name |
| `type` | String | Yes | `BANK`, `CASH`, or `WALLET` |
| `openingBalancePaise` | Long | Yes | Starting balance in paise |
| `colorValue` | Long | Yes | Packed Compose color value |
| `isArchived` | Boolean | Yes | Hidden/active state |
| `createdAtMillis` | Long | Yes | Unix epoch milliseconds |
| `updatedAtMillis` | Long | Yes | Unix epoch milliseconds |

### `expense_transactions`

Stores debit, credit, and transfer records.

| Column | Type | Required | Notes |
|---|---|---:|---|
| `id` | Long | Yes | Auto-generated primary key |
| `type` | String | Yes | `DEBIT`, `CREDIT`, or `TRANSFER` |
| `amountPaise` | Long | Yes | Positive amount in paise |
| `sourceAccountId` | Long | No | Required for debit and transfer |
| `destinationAccountId` | Long | No | Required for credit and transfer |
| `category` | String | Yes | Expense/income category enum name |
| `paymentMethod` | String | No | UPI, debit card, cash, net banking, or other |
| `transactionDateEpochDay` | Long | Yes | Local transaction date |
| `transactionTimeMinutes` | Int | No | Minutes after midnight |
| `note` | String | No | Optional note |
| `createdAtMillis` | Long | Yes | Unix epoch milliseconds |
| `updatedAtMillis` | Long | Yes | Unix epoch milliseconds |

Foreign keys:

- `sourceAccountId` -> `expense_accounts.id`, delete restricted
- `destinationAccountId` -> `expense_accounts.id`, delete restricted

Indexes:

- `sourceAccountId`
- `destinationAccountId`
- `transactionDateEpochDay`
- `type`

Balance calculation:

```text
current balance = opening balance
                + credits into the account
                + transfers into the account
                - debits from the account
                - transfers out of the account
```

A transfer changes two account balances but does not change the combined balance.

## 3. Proto DataStore

`AppSettings` fields:

| Field | Purpose |
|---|---|
| `onboarding_completed` | Determines first-run navigation |
| `theme_mode` | System, Light, or Dark |
| `reminder_sound` | Global reminder sound preference |
| `default_snooze_minutes` | 5, 10, 15, 30, or 60 minutes |
| `quiet_hours_enabled` | Whether interval reminders are deferred during Quiet Hours; defaults to true |
| `quiet_hours_start_minutes` | Quiet Hours start as minutes after midnight; defaults to 1380 (23:00) |
| `quiet_hours_end_minutes` | Quiet Hours end as minutes after midnight; defaults to 360 (06:00) |
| `app_lock_enabled` | App Lock state |
| `app_lock_passcode_hash` | Salted PBKDF2 result |
| `app_lock_passcode_salt` | Random salt |
| `app_lock_recovery_hash` | Salted PBKDF2 result |
| `app_lock_recovery_salt` | Random salt |
| `app_lock_biometric_enabled` | Optional biometric unlock preference |

Plaintext passcodes and recovery codes are never persisted.

## 4. Backup Schema

Current export format is UTF-8 JSON with schema version 4. It contains:

- Export timestamp
- User profile
- Base64-encoded profile photo, when present
- Reminders
- Ledger entries
- Activity logs
- Expense accounts
- Expense transactions
- Normal app preferences

App Lock credentials are stored separately from `AppPreferences` and are excluded.

Import protections include:

- Maximum total file size
- Supported schema range check
- Required profile/name validation
- Enum validation
- Record-count limits
- Profile-photo size validation
- Expense foreign-key validation
- Snooze preference validation
- Quiet Hours range and unequal-start/end validation
- Room transaction for record replacement
- Alarm cancellation and Quiet Hours-aware reconciliation after success

Schema evolution:

- Schema 1: profile, reminders, ledger, activity, and settings
- Schema 2: expense accounts and transactions
- Schema 3: profile photo content
- Schema 4: Quiet Hours preferences and per-interval reminder overrides

The existing manual JSON export is local and user-controlled. Encrypted Google Drive backup is planned for the next phase and must encrypt the validated snapshot before upload.

## 5. Migration Policy

- Every Room schema change requires an explicit migration.
- Migrations must preserve user data.
- Exported Room schema JSON must be committed.
- Migration tests must cover the complete supported chain.
- Destructive migration is not permitted in production.
- New enum values must remain backward compatible with backup parsing.

Current migration chain supports versions 1 through 7. Migration 6 to 7 adds the non-null `allowDuringQuietHours` reminder column with a false default so existing reminders retain sleep-friendly behavior.
