# ROADMAP.md

# Raachi Memory v1.0 Development Roadmap

**Project Status:** Architecture Locked

------------------------------------------------------------------------

# Development Rules

-   Complete one phase before starting the next.
-   Every phase must build successfully.
-   Do not skip testing.
-   No new features unless approved.
-   Follow ARCHITECTURE.md, DATABASE.md and UI_GUIDELINES.md.

------------------------------------------------------------------------

# Phase 0 -- Project Initialization

## Goal

Create the project foundation.

### Tasks

-   Create Android project
-   Configure Gradle
-   Enable Jetpack Compose
-   Configure Material 3
-   Add Nunito font
-   Configure Hilt
-   Configure Room
-   Configure Navigation Compose
-   Configure DataStore
-   Configure WorkManager
-   Configure AlarmManager
-   Create Git repository

**Deliverable:** App launches successfully with empty home.

------------------------------------------------------------------------

# Phase 1 -- Design System

## Goal

Build reusable UI components.

### Tasks

-   App Theme
-   Color scheme
-   Typography
-   Card components
-   Buttons
-   Text fields
-   Dialogs
-   Bottom sheets
-   Snackbar
-   Loading indicators

**Deliverable:** Shared design system ready.

------------------------------------------------------------------------

# Phase 2 -- Database Layer

## Goal

Implement Room.

### Tasks

-   User Profile Entity
-   Reminder Entity
-   Ledger Entity
-   Activity Entity
-   Settings Entity
-   DAO classes
-   Repository interfaces
-   Migration setup

**Deliverable:** Local database working.

------------------------------------------------------------------------

# Phase 3 -- Profile & Onboarding

## Goal

Collect user information.

### Tasks

-   Splash Screen
-   Welcome Screen
-   Name Screen (Required)
-   Optional Profile Screen
-   BMI Card
-   Profile Page

**Deliverable:** User reaches Dashboard.

------------------------------------------------------------------------

# Phase 4 -- Dashboard

## Goal

Create the home experience.

### Tasks

-   Greeting
-   Up Next Card
-   Pending Ledger Card
-   Today's Summary
-   Floating Action Button
-   Bottom Navigation

**Deliverable:** Fully functional dashboard.

------------------------------------------------------------------------

# Phase 5 -- Reminder Module

## Goal

Reminder management.

### Tasks

-   Create Reminder
-   Edit Reminder
-   Delete Reminder
-   Repeat Rules
-   Alarm Scheduling
-   Snooze
-   Done
-   Skip

**Deliverable:** Reminder system complete.

------------------------------------------------------------------------

# Phase 6 -- Ledger Module

## Goal

Track money and belongings.

### Tasks

-   Add Entry
-   Edit Entry
-   Mark Returned
-   Due Date
-   Overdue Alerts
-   WhatsApp Share
-   History

**Deliverable:** Ledger module complete.

------------------------------------------------------------------------

# Phase 7 -- Notification System

## Goal

Reliable notifications.

### Tasks

-   Health Alarm Notifications
-   Ledger Notifications
-   Notification Actions
-   Notification Channels
-   Notification Permissions

**Deliverable:** Stable notification engine.

------------------------------------------------------------------------

# Phase 8 -- Activity Module

## Goal

Timeline of events.

### Tasks

-   Activity Screen
-   Filters
-   Search
-   Event Logging

**Deliverable:** Complete activity history.

------------------------------------------------------------------------

# Phase 9 -- Settings

## Goal

Application preferences.

### Tasks

-   Dark Mode
-   Reminder Sound
-   Default Snooze
-   Export JSON
-   Import JSON
-   About

**Deliverable:** Settings completed.

------------------------------------------------------------------------

# Phase 10 -- Testing

## Tasks

-   Unit Testing
-   Manual Testing
-   UI Testing
-   Alarm Testing
-   Database Testing
-   Dark Theme Testing
-   Daily Expense Manager tests

**Deliverable:** Release Candidate.

------------------------------------------------------------------------

# Phase 11 -- Play Store Release

## Tasks

-   App Icon
-   Feature Graphic
-   Screenshots
-   Privacy Policy
-   Play Store Listing
-   Internal Testing
-   Production Release

**Deliverable:** Raachi Memory v1.0 published.

------------------------------------------------------------------------

# Future (Not in v1)

-   Cloud Sync
-   Widgets
-   Voice Input
-   AI Quick Capture
-   Budgets and spending charts
-   Expense CSV export

------------------------------------------------------------------------

# Success Criteria

Version 1.0 is complete when:

-   All reminders work offline.
-   Ledger reminders work correctly.
-   WhatsApp sharing works.
-   Export/Import works.
-   No crashes during normal usage.
-   App is accepted on Google Play Store.

------------------------------------------------------------------------

**Guiding Principle**

> Build a simple, reliable and beautiful app that helps users **never
> forget what matters**.
