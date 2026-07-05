package com.raachi.memory.domain.model

enum class Gender { MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY }

enum class ReminderCategory { WATER, MEDICINE, BREAKFAST, LUNCH, DINNER, EXERCISE, SLEEP, CUSTOM }

enum class ReminderType { ONE_TIME, DAILY, WEEKLY, INTERVAL }

enum class ReminderStatus { ACTIVE, COMPLETED, SKIPPED, ARCHIVED }

enum class LedgerStatus { PENDING, RETURNED }

enum class ItemType { MONEY, BOOK, CHARGER, DOCUMENTS, OTHER }

enum class EventType { REMINDER_COMPLETED, REMINDER_SNOOZED, LEDGER_REMINDER_SENT, MONEY_RETURNED, ITEM_RETURNED, OTHER }