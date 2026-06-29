# DATABASE.md
# Raachi Memory v1.0 Database Design
## Philosophy
-   Offline First
-   Room Database is the single source of truth.
-   Every feature stores data locally.
-   Future cloud sync will synchronize with Room instead of replacing it.
------------------------------------------------------------------------

# Database Overview
Tables:

1.  user_profile
2.  reminders
3.  ledger_entries
4.  activity_logs
5.  app_settings

------------------------------------------------------------------------

# Entity: user_profile
Purpose: Stores user information.

Fields:
-   id (Primary Key)
-   name (Required)
-   gender
-   age
-   birthday
-   email
-   mobile
-   height_cm
-   weight_kg
-   profile_photo_uri
-   created_at
-   updated_at

Notes: BMI is calculated dynamically and not stored.

------------------------------------------------------------------------

# Entity: reminders
Purpose: Stores all reminder information.
Fields:

-   id
-   title
-   category
-   description
-   reminder_type
-   repeat_type
-   interval_hours
-   scheduled_time
-   next_trigger
-   ringtone
-   vibration_enabled
-   status
-   created_at
-   updated_at

Categories:
-   Water
-   Medicine
-   Breakfast
-   Lunch
-   Dinner
-   Exercise
-   Sleep
-   Custom

Status:
-   Active
-   Completed
-   Skipped
-   Archived

------------------------------------------------------------------------

# Entity: ledger_entries

Purpose: Tracks money and belongings.
Fields:
-   id
-   person_name
-   mobile_number
-   item_type
-   item_name
-   amount
-   due_date
-   returned
-   returned_date
-   notes
-   created_at

Item Types:

-   Money
-   Book
-   Charger
-   Documents
-   Other

------------------------------------------------------------------------

# Entity: activity_logs
Purpose: Stores application history.
Fields:
-   id
-   event_type
-   reference_id
-   title
-   description
-   event_time

Examples:
-   Reminder Completed
-   Reminder Snoozed
-   Ledger Reminder Sent
-   Money Returned

------------------------------------------------------------------------

# Entity: app_settings
Stores application preferences.

Fields:
-   dark_mode
-   reminder_sound
-   default_snooze_minutes
-   notifications_enabled
-   first_launch_completed

------------------------------------------------------------------------

# Relationships
user_profile \| +-- app_settings
reminders \| +-- activity_logs
ledger_entries \| +-- activity_logs

------------------------------------------------------------------------

# Indexes
Recommended indexes:
-   reminders.next_trigger
-   reminders.status
-   ledger_entries.due_date
-   ledger_entries.returned
-   activity_logs.event_time

------------------------------------------------------------------------

# Backup Strategy
Export Format: JSON
Contains:
-   Profile
-   Reminders
-   Ledger
-   Activity
-   Settings

------------------------------------------------------------------------
# Migration Strategy

Use Room Migration for schema updates.
Never delete user data during migration.

------------------------------------------------------------------------

# Future Tables (Not in v1)
-   cloud_sync
-   reminder_templates
-   widgets

------------------------------------------------------------------------

# Summary
The database is intentionally small, easy to maintain, and optimized for
offline use.

Room remains the only source of truth throughout the application
lifecycle.
