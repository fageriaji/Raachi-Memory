You are a Senior Android Engineer working on the existing Android Studio project Raachi Memory.

The project already exists.

Do NOT recreate the project.

Before writing any code, carefully read and follow:

• MASTER_PRD.md
• ARCHITECTURE.md
• DATABASE.md
• UI_GUIDELINES.md
• ROADMAP.md
• CODING_STANDARDS.md

These documents are the project's source of truth.

Do not modify them.

==================================================
PROJECT GOAL
==================================================

Implement Phase 7 of Raachi Memory.

Only implement the functionality requested in the current milestone.

Do not begin future milestones automatically.

==================================================
ARCHITECTURE
==================================================

Follow the existing project architecture exactly.

Use:

• Clean Architecture
• MVVM
• Repository Pattern
• Room
• Hilt
• StateFlow / Flow
• AlarmManager
• Material 3

Do not introduce new architectural patterns.

Do not change the existing package structure.

Do not recreate existing modules.

Do not modify completed phases unless absolutely required.

==================================================
CODE QUALITY
==================================================

Follow SOLID principles.

Keep classes focused on a single responsibility.

Avoid duplicate business logic.

Prefer reusable utility functions over copy-paste.

Repositories coordinate data sources.

Business logic belongs in Repository / Use Cases.

Never place business logic inside Composables.

UI observes StateFlow only.

Do not access Room directly from the UI layer.

Reuse existing project utilities whenever possible.

Use existing DateTimeUtils for all date/time formatting and calculations.

==================================================
LOCALIZATION
==================================================

Never hard-code user-visible text.

All UI strings, validation messages, notifications,
dialogs, WhatsApp messages, filter labels,
button labels and status labels must come from
strings.xml.

Reuse existing string resources whenever possible.

Create new string resources only when necessary.

==================================================
ITEM TYPE
==================================================

ItemType must be implemented as a strongly typed enum.

Never use raw strings.

==================================================
LEDGER STATUS
==================================================

Only these persisted statuses exist:

• Pending
• Returned

IMPORTANT

Overdue is NOT stored in Room.

Overdue is derived automatically whenever:

Current DateTime > DueDateTime

AND

Status == Pending

No background database update should be required.

==================================================
DASHBOARD
==================================================

Dashboard must observe Repository Flow only.

No manual refresh.

Upcoming Due Entries are:

DueDateTime >= Current DateTime

AND

DueDateTime <= Current DateTime + 72 Hours

AND

Status == Pending

==================================================
REMINDER RULES
==================================================

Each Ledger Entry schedules:

1.

Due Reminder

Exactly at:

Due Date
+
Due Time

2.

Overdue Reminder

Every day at 9:00 AM

until marked Returned.

Editing Due Date or Due Time:

• Cancel previous alarms
• Schedule new alarms

Deleting an entry:

• Cancel alarms
• Cancel notifications

Mark Returned:

• Cancel alarms
• Cancel notifications

Support device reboot restoration.

==================================================
NOTIFICATIONS
==================================================

Notification IDs must be deterministic using Ledger ID.

Alarm Request Codes must be deterministic using Ledger ID.

Notification Actions:

• Mark Returned
• Snooze (1 Day)
• Share

Snooze delays only the next reminder by 24 hours.

It must NOT modify:

• Due Date
• Due Time
• Ledger Status

==================================================
WHATSAPP
==================================================

Prefer launching WhatsApp directly.

If WhatsApp is unavailable,

automatically fall back to the Android Sharesheet.

Never display an error simply because WhatsApp is unavailable.

==================================================
ERROR HANDLING
==================================================

Handle failures gracefully.

Follow the existing project's error handling pattern.

If the project already uses Result, Resource, UiState,
or another standardized error model, reuse it.

Do not introduce a new error handling pattern.

Repository methods shall never throw unhandled exceptions to the UI layer.

Handle failures including:

• Invalid input
• Alarm scheduling failure
• Notification failure
• Missing WhatsApp
• Database failures
• Permission-related failures (where applicable)

Never crash the application because of recoverable errors.

Provide user-friendly feedback using the existing Snackbar or UI messaging system.

Log unexpected exceptions using the existing project logging approach.

==================================================
EXISTING CODE PROTECTION
==================================================

Do not rewrite existing working code unless required for the current milestone.

Do not perform cosmetic refactoring.

Do not rename existing classes,
packages,
methods,
variables,
resources,
or files unless necessary.

Minimize modifications to existing files.

Prefer extending the existing implementation rather than replacing it.

All previous phases must continue working exactly as before.

==================================================
BUILD REQUIREMENT
==================================================

The project must compile successfully after every milestone.

Do not leave:

• TODOs
• Stub implementations
• Placeholder code
• Unused classes
• Unused resources

Do not introduce compiler warnings.

Do not introduce lint warnings where reasonably avoidable.

If a new dependency is required:

• Explain why it is needed.
• Justify why the existing project cannot solve the problem without it.
• Wait for approval before introducing it.

Never add dependencies for convenience alone.

Prefer the existing project libraries and utilities whenever possible.

==================================================
DELIVERABLES
==================================================

After completing the requested milestone, return:

1. Summary

2. Updated File Tree

3. Modified Files

4. New Files

5. Manifest Changes (if any)

6. Build Verification

Do not continue to the next milestone automatically.

Wait for review before proceeding.