# Raachi Memory Roadmap

## Current Status

- Current application version: `1.0.3` (`versionCode 3`)
- Core offline application: complete
- Room schema: 7
- Backup schema: 4
- Current focus: final release QA and Play Store preparation
- Next approved product phase: encrypted Google Drive backup

## Delivery Rules

- Complete and verify each milestone before starting the next.
- Preserve existing local data and migration compatibility.
- Keep Room as the local source of truth.
- Do not make login or connectivity mandatory.
- Use test credentials, test ads, and non-production services during development.
- Update this roadmap, the PRD, architecture, database, UI guide, and test report when behavior changes.

## Completed Milestones

### Foundation and Design System

- Kotlin Android application and Gradle setup
- Jetpack Compose and Material 3
- Bundled Nunito fonts
- Brand light/dark color schemes
- Hilt, Room, Proto DataStore, Navigation Compose, and WorkManager dependencies
- Adaptive, round, themed, legacy, splash, onboarding, and About logo assets

### Onboarding and Profile

- Branded splash and welcome experience
- Required-name onboarding
- Optional date of birth, phone, gender, email, height, and weight
- Field validation and native date picker
- Profile and BMI presentation
- Native photo selection, crop, app-private persistence, change, and removal

### Dashboard and Navigation

- Time-aware greeting and current day/date
- Reminder, ledger, and completion summaries
- Category-aware reminder visuals
- Pending ledger preview
- Quick-add actions
- Profile shortcut
- Modal navigation drawer
- Four-item bottom navigation
- Consistent hamburger/back-button behavior and screen titles

### Reminder System

- Add, edit, and delete
- One-time, daily, weekly, and interval schedules
- Future date/time validation
- Active, Snoozed, and Completed tabs
- Exact alarms and permission controls
- Snooze, Done, and Skip notification actions
- Custom Raachi notification sound
- Enabled-by-default, user-configurable Quiet Hours for interval reminders
- Per-interval Quiet Hours override and persisted next-deliverable scheduling
- Reboot, package-update, time-change, and timezone reconciliation

### Ledger

- Money and item records
- Lent and Borrowed directions
- Transaction and due dates
- Edit, delete, and mark returned
- Due/overdue alarms
- WhatsApp, WhatsApp Business, and share-sheet behavior
- Android contact picker for name and phone number

### Activity and Settings

- Searchable reminder/ledger activity timeline
- Reminder and ledger filters
- System, Light, and Dark themes
- Global reminder sound control
- Default snooze selection
- Quiet Hours controls under Reminder Preferences
- Notification and exact-alarm controls
- About screen

### Local Backup and Restore

- Timestamped JSON export
- Validated import
- Backup schemas 1 through 4
- Profile photo backup
- Expense data backup
- Transactional record replacement
- Alarm rescheduling after restore

### Daily Expense Manager

- Bank, Cash, and Wallet accounts
- Opening balances and current-balance calculation
- Debit, Credit, and Transfer transactions
- Categories, payment methods, dates, times, and notes
- Date and transaction-type filters
- Edit/delete workflows and insufficient-funds validation
- Responsive light/dark expense overview design

### App Lock and Security

- Custom 6-digit passcode
- Optional biometric unlock
- Secure device-credential reset path
- Generated and hashed recovery code
- PBKDF2-based credential storage
- Lock-on-background behavior
- Correct protected flow through Photo Picker and crop UI

### v1.0.3 Stabilization

- Navigation state corrections from Profile and drawer routes
- Compact Reminder and Ledger cards
- Dark-theme status-bar and logo visibility corrections
- Drawer sizing and profile-header refinement
- Stable named resource URI for the custom notification sound
- Profile crop content-URI and App Lock lifecycle fixes
- Source committed and tagged as `v1.0.3`

## Current Milestone: Release QA

### Required Checks

- Regression test all create, edit, delete, filter, and navigation paths.
- Test reminders and ledger alerts on physical devices.
- Test reboot, shutdown, Doze, battery saver, manual time change, and timezone change.
- Test Android 8 through current supported Android versions where practical.
- Test manufacturer battery restrictions, especially Samsung devices.
- Verify notification actions and the bundled custom sound after clean install and upgrade.
- Verify App Lock, biometrics, device-credential reset, and recovery-code reset.
- Verify local backup schemas 1, 2, 3, and 4.
- Check small screens, large fonts, TalkBack, contrast, and touch targets.
- Build and test the minified signed release artifact.

### Store Preparation

- Secure release keystore and signing configuration
- Signed Android App Bundle
- 512x512 store icon and feature graphic
- Final screenshots
- Store title, descriptions, and release notes
- Hosted privacy policy
- Data Safety, content rating, target audience, notification, and exact-alarm declarations
- Play Console internal testing and pre-launch report

## Next Phase: Encrypted Google Drive Backup

This phase starts only after explicit approval.

Planned scope:

- Optional Google account connection
- Limited Google Drive `appDataFolder` authorization
- AES-256-GCM encrypted backup container
- Separate high-entropy cloud backup recovery phrase
- Android Keystore protection for unattended local access to the backup key
- Manual **Back up now** action
- Approximate weekly WorkManager backup
- Network/battery constraints and optional Wi-Fi-only mode
- Four verified rolling backups
- Upload-before-delete rotation so a failed backup never removes a good copy
- Restore preview, validation, transactional import, and alarm rebuilding
- Authorization-expired and recovery-key-lost handling

Room remains the source of truth; this phase is backup and restore, not live synchronization.

## Later Candidates

Priorities should be validated with real users before implementation.

- Expense budgets, recurring transactions, reports, and CSV/PDF export
- Home-screen widgets and quick-add actions
- Global search
- Restrained AdMob integration with consent and an ad-free option
- SMS-assisted expense detection, subject to Google Play permission approval
- Voice input
- Multi-device synchronization and shared records
- AI-assisted capture

## Explicitly Not Current Scope

- Mandatory login
- Server-readable personal data
- Direct bank credential access
- Unreviewed SMS collection
- Ads on onboarding, App Lock, editor, alarm, or sensitive financial-entry flows

## Release Success Criteria

- No known critical crash or data-loss path.
- Reminder and ledger alarms remain reliable across device lifecycle events.
- Expense balances remain correct after edits, deletes, and transfers.
- Backup import restores all supported data and reschedules alarms.
- App Lock cannot be bypassed through normal navigation or media-picker flows.
- Store policy and privacy declarations match actual application behavior.
