# ARCHITECTURE.md

# Raachi Memory v1.0

**Tagline:** Never forget what matters.

## 1. Project Overview

Raachi Memory is an offline-first Android application for remembering
health routines and money/items lent to others.

## 2. Vision

A calm, modern, privacy-first personal memory companion.

## 3. Goals

-   Offline First
-   Beautiful Material 3 UI
-   Reliable reminders
-   No mandatory login
-   Play Store ready

### Non Goals

-   Expense tracker
-   Habit tracker
-   AI chat
-   Cloud sync (v1)

## 4. Core Principles

-   Room is the single source of truth.
-   Business logic never lives in Composables.
-   User owns all data.
-   Simple over complex.

## 5. Technology Stack

-   Kotlin
-   Jetpack Compose
-   Material 3
-   Nunito fonts from res/font
-   Room
-   Hilt
-   Navigation Compose
-   AlarmManager
-   WorkManager
-   DataStore
-   Coroutines + Flow

## 6. High Level Architecture

UI → ViewModel → UseCases → Repository → Room

AlarmManager handles exact reminders. DataStore stores preferences.

## 7. Project Structure

app/ - core - features - data - domain - di

Features: Dashboard, Reminder, Ledger, Activity, Profile, Settings.

## 8. Modules

Dashboard: Greeting, reminders, ledger, summary. Reminder: Water,
medicine, meals, sleep, custom. Ledger: Money/items, due dates, WhatsApp
share. Activity: Timeline. Profile: Name(required), optional profile,
BMI card. Settings: Theme, sounds, export/import.

## 9. Navigation

Splash → Onboarding → Dashboard. Dashboard navigates to all modules. FAB
creates Reminder or Ledger.

## 10. Data Flow

Compose → ViewModel → UseCase → Repository → Room → UI.

## 11. Notifications

Health: Alarm + Snooze + Done. Ledger: Notification + WhatsApp + Mark
Returned.

## 12. Offline Strategy

Everything works without internet. Future sync synchronizes against
Room.

## 13. Privacy

No forced login. Minimal permissions. Export/import supported.

## 14. Performance

LazyColumn, StateFlow, lightweight animations.

## 15. ADRs

ADR-001 Kotlin. ADR-002 Compose. ADR-003 Room. ADR-004 Poppins.

## 16. Future

Widgets, cloud backup, voice input.

## 17. Summary

Every architectural decision must support the promise: **Never forget
what matters.**
