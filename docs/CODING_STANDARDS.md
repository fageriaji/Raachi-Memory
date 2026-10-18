# Raachi Memory Coding Standards

## 1. General

- Use Kotlin for application code.
- Prefer simple, explicit code over speculative abstraction.
- Keep changes scoped to the approved feature or defect.
- Preserve established package, naming, and UI patterns.
- Do not leave placeholders, dead branches, or unresolved TODOs in release code.

## 2. Architecture

The expected dependency direction is:

```text
Compose -> ViewModel -> use case/manager -> repository -> Room/DataStore/platform
```

- Composables must not access DAOs, DataStore, or schedulers directly.
- Business rules belong in domain use cases, managers, or pure functions.
- Repository interfaces belong in the domain layer.
- Platform-backed implementations belong in the data layer.
- ViewModels coordinate UI state and application operations; they should not contain Android UI code.
- Room remains the source of truth for application records.

## 3. Kotlin

- Prefer immutable data classes and read-only collections.
- Use typed enums instead of free-form strings in domain code.
- Use `java.time` types in domain models; convert only at persistence and UI boundaries.
- Model money as `Long` paise, never `Float` or `Double`.
- Use meaningful names and small functions with one clear responsibility.
- Add KDoc for public APIs when behavior, constraints, or security implications are not obvious.
- Avoid `!!`; validate or model nullability deliberately.

## 4. Coroutines and Flow

- Use structured concurrency.
- Use `viewModelScope` for ViewModel work.
- Never use `GlobalScope`.
- Expose observable UI state through `StateFlow`.
- Collect flows in Compose with lifecycle-aware APIs.
- Run file, cryptographic, and blocking operations away from the main thread.
- Let Room suspend APIs and transactions manage their own database dispatching where appropriate.

## 5. Jetpack Compose

- Hoist state and pass events down.
- Keep Composables side-effect aware and lifecycle safe.
- Put all user-visible text in `strings.xml`.
- Use stable item keys in lazy lists.
- Provide content descriptions for meaningful icons and images.
- Keep touch targets at least 48dp unless a platform component guarantees accessibility.
- Test compact screens and large font scales.
- Use theme tokens instead of local hard-coded surface/text colors.
- Follow `UI_GUIDELINES.md` for navigation, typography, color roles, and card treatment.

Brand colors that encode domain meaning may be declared centrally in the theme/design-system code. They should not be duplicated across screens.

## 6. Persistence

- Every Room schema change requires an explicit migration.
- Never use destructive migration in production.
- Commit exported Room schemas.
- Add migration tests for the complete supported chain.
- Keep enum persistence names stable or provide a migration path.
- Validate foreign-key and cross-record references during imports.
- Preserve backup import compatibility for all documented supported schemas.

## 7. Alarms and Notifications

- Alarm scheduling must be idempotent.
- `PendingIntent` identity and request codes must remain stable for an existing record.
- Receivers must tolerate deleted, completed, or missing records.
- Notification actions must update data, activity history, and future scheduling consistently.
- Use stable named resource URIs for bundled notification sounds.
- Notification-channel changes require a deliberate channel-version migration.
- Exact alarms must not be replaced by periodic WorkManager jobs.

## 8. Security and Privacy

- Request the minimum permission required at the moment it is needed.
- Prefer Android Photo Picker, contact picker, Sharesheet, and other scoped system APIs.
- Never log passcodes, recovery codes, profile data, phone numbers, financial values, backup plaintext, or authentication tokens.
- Store credential verifiers as salted slow hashes.
- Use established platform cryptography; do not design custom ciphers.
- Treat backup import, external intents, and URIs as untrusted input.
- Audit third-party SDK permissions and data behavior before inclusion.

## 9. Dependency Injection

- Use Hilt constructor injection by default.
- Use modules for framework-created objects and interface bindings.
- Avoid service locators and static mutable application state.

## 10. Testing

- Add unit tests for validation, recurrence, calculations, mapping, and security logic.
- Add DAO and migration instrumentation tests for schema changes.
- Add UI tests for critical navigation and state-restoration paths when the platform runner supports them.
- Perform physical-device testing for alarms, notification channels, sound, biometrics, reboot, Doze, and manufacturer restrictions.
- Run unit tests, lint, debug build, and minified release build before a release commit.

## 11. Git and Documentation

- Use focused commits with meaningful imperative messages.
- Do not mix unrelated refactors with a feature or bug fix.
- Update documentation in the same change when behavior, schema, permissions, security, or release status changes.
- Keep rollback tags for released versions.
