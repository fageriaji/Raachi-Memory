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
5.  expense_accounts
6.  expense_transactions

App settings are stored separately in DataStore. The current Room schema version is 6.

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
-   transaction_date (required lent/borrowed date)
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

# DataStore: app_settings
Stores application preferences outside Room.

Fields:
-   dark_mode
-   reminder_sound
-   default_snooze_minutes
-   notifications_enabled
-   first_launch_completed

------------------------------------------------------------------------

# Entity: expense_accounts
Purpose: Stores each bank, cash, or wallet balance source.

Fields:
-   id
-   name
-   account_type
-   opening_balance_paise
-   color_value
-   is_archived
-   created_at
-   updated_at

------------------------------------------------------------------------

# Entity: expense_transactions
Purpose: Stores debit, credit, and transfer entries.

Fields:
-   id
-   transaction_type
-   amount_paise
-   category
-   source_account_id
-   destination_account_id
-   payment_method
-   note
-   transaction_date
-   transaction_time
-   created_at
-   updated_at

Amounts are stored as integer paise. Transfers debit the source and credit the destination, so they do not change the combined balance.

------------------------------------------------------------------------

# Relationships
user_profile \| +-- app_settings
reminders \| +-- activity_logs
ledger_entries \| +-- activity_logs
expense_accounts \| +-- expense_transactions

------------------------------------------------------------------------

# Indexes
Recommended indexes:
-   reminders.next_trigger
-   reminders.status
-   ledger_entries.due_date
-   ledger_entries.returned
-   activity_logs.event_time
-   expense_transactions.transaction_date
-   expense_transactions.source_account_id
-   expense_transactions.destination_account_id

------------------------------------------------------------------------

# Backup Strategy
Export Format: JSON
Contains:
-   Profile
-   Reminders
-   Ledger
-   Activity
-   Settings
-   Expense Accounts
-   Expense Transactions

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
