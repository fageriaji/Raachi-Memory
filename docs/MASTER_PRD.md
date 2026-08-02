# MASTER_PRD.md

# Raachi Memory v1.0

**Master Product Requirements Document**

**Tagline:** Never forget what matters.

------------------------------------------------------------------------

# 1. Product Summary

Raachi Memory is an offline-first Android application that helps users
remember important daily activities and personal commitments.

Primary use cases: - Drink water - Take medicine - Eat meals -
Exercise - Sleep - Track money lent - Track belongings lent - Track daily income and expenses

------------------------------------------------------------------------

# 2. Vision

Create a simple, beautiful and reliable personal memory companion that
works completely offline and respects user privacy.

------------------------------------------------------------------------

# 3. Target Users

-   Individuals who frequently forget daily routines.
-   Family members.
-   Students.
-   Working professionals.
-   Elderly users needing medicine reminders.

------------------------------------------------------------------------

# 4. Core Features (v1.0)

## Dashboard

-   Greeting
-   Upcoming reminders
-   Pending ledger
-   Today's summary
-   Quick Add

## Smart Reminders

-   One-time
-   Daily
-   Weekly
-   Every X hours
-   Snooze
-   Done
-   Skip

## Ledger

-   Money
-   Belongings
-   Due dates
-   Overdue reminders
-   WhatsApp share
-   Phone contact picker without broad contacts permission
-   Mark returned

## Activity

Timeline of reminder and ledger events.

## Daily Expense Manager

-   Multiple bank, cash, and wallet accounts
-   Opening balance per account
-   Debit and credit transactions
-   Transfers between accounts without changing the combined balance
-   Account and date filters
-   Local-only financial data; no bank credentials or bank integration

## Profile

Required: - Name

Optional: - Birthday - Gender - Age - Height - Weight - Email - Mobile

BMI is calculated from height and weight.

## Settings

-   Dark mode
-   Reminder sound
-   Default snooze
-   Export JSON
-   Import JSON
-   About

------------------------------------------------------------------------

# 5. Out of Scope (v1.0)

-   Cloud Sync
-   Firebase
-   AI Chat
-   Habit Tracking
-   Budgets and spending charts
-   Notes
-   Ads
-   Subscription

------------------------------------------------------------------------

# 6. User Journey

Splash → Welcome → Name → Optional Profile → Dashboard → Daily Usage

------------------------------------------------------------------------

# 7. Design Principles

-   Material 3
-   Google Nunito (Locked and Final)
-   Card-based interface
-   Smooth animations
-   Modern UI
-   Accessible
-   Offline-first

------------------------------------------------------------------------

# 8. Technical Principles

-   Kotlin
-   Jetpack Compose
-   Clean Architecture
-   MVVM
-   Room Database
-   Hilt
-   AlarmManager
-   WorkManager
-   DataStore

------------------------------------------------------------------------

# 9. Documentation References

This document references:

-   ARCHITECTURE.md
-   DATABASE.md
-   UI_GUIDELINES.md
-   ROADMAP.md
-   CODING_STANDARDS.md
-   AI_PROMPT_GUIDE.md

These documents must always remain synchronized.

------------------------------------------------------------------------

# 10. Definition of Done

Version 1.0 is complete when:

✓ All reminder types work offline.

✓ Alarm notifications are reliable.

✓ Ledger reminders work.

✓ WhatsApp sharing works.

✓ Export & Import work.

✓ Dark mode supported.

✓ No critical crashes.

✓ Published on Google Play Store.

------------------------------------------------------------------------

# 11. Acceptance Criteria

The application should:

-   Launch quickly.
-   Require no account.
-   Store all data locally.
-   Feel modern and easy to use.
-   Help users remember important things with minimal effort.

------------------------------------------------------------------------

# 12. Success Metrics

-   Reliable reminders.
-   Easy reminder creation.
-   Simple ledger tracking.
-   Positive feedback from family and friends.
-   Stable Play Store release.

------------------------------------------------------------------------

# 13. Guiding Principle

Every design and engineering decision must support one promise:

> **Never forget what matters.**

If a feature adds complexity without strengthening this promise, it
should not be included in Version 1.0.
