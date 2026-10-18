# Raachi Memory Implementation Rules

## 1. Scope Control

- Implement only explicitly approved behavior.
- Preserve working behavior outside the requested area.
- Prefer extending established code over replacing a complete subsystem.
- Do not combine release refinement with unrelated new features.
- Defer cloud backup, ads, SMS access, and sync until their phase is approved.

## 2. Data Protection

- Never delete or overwrite user data without validation and a clear user action.
- Every Room change requires a migration and migration test.
- Backup imports must validate the complete payload before replacement.
- Restore operations must rebuild reminder and ledger alarms.
- Keep App Lock credentials outside normal backup exports.
- Do not silently enable Android automatic backup or cloud transmission.

## 3. Architecture

- Keep business logic outside Composables.
- Keep UI state in ViewModels.
- Keep persistence and platform operations behind repositories/managers.
- Keep Room as the local source of truth.
- Use AlarmManager for exact user reminders and WorkManager for deferrable persistent work only.

## 4. UI and Navigation

- Follow the implemented drawer and four-item bottom navigation model.
- Primary screens use a hamburger menu; editor/detail screens use a back action.
- Screen-title sizing and alignment must remain consistent.
- Reuse centralized theme colors, typography, spacing, and components.
- Support light, dark, and system themes.
- Verify small screens and large text before completion.

## 5. Permissions and External Data

- Request only permissions declared in the approved feature design.
- Prefer system pickers over broad contacts, photos, or storage permissions.
- Treat incoming URIs, backup files, contact data, and external-app results as untrusted.
- Any new internet, SMS, contacts, location, camera, or microphone permission requires a privacy and Play policy review.

## 6. Build Quality

- No unresolved TODOs or placeholder implementation.
- No unnecessary dependency additions.
- Debug and release variants must compile.
- R8/resource shrinking must remain enabled for release.
- Lint errors must be fixed; advisory warnings must be reviewed and documented when accepted.

## 7. Verification

For each milestone:

1. Review the affected source, schema, and documentation.
2. Implement the smallest complete change.
3. Add or update automated tests proportional to risk.
4. Run unit tests and relevant instrumentation tests.
5. Run lint and both debug/minified release builds for release-impacting changes.
6. Test device-dependent behavior on an emulator or physical device.
7. Update `TEST_REPORT.md` and other affected docs.
8. Commit only after user approval when approval is requested.

## 8. Completion Standard

A phase is complete only when its normal flow, empty state, validation errors, permission denial, failure handling, dark theme, and data-preservation behavior have been checked.
