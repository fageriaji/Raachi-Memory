package com.raachi.memory.data.local.converter

import androidx.room.TypeConverter
import com.raachi.memory.domain.model.EventType
import com.raachi.memory.domain.model.Gender
import com.raachi.memory.domain.model.ItemType
import com.raachi.memory.domain.model.LedgerStatus
import com.raachi.memory.domain.model.ReminderCategory
import com.raachi.memory.domain.model.ReminderStatus
import com.raachi.memory.domain.model.ReminderType

class Converters {
    @TypeConverter
    fun fromGender(value: Gender?): String? = value?.name

    @TypeConverter
    fun toGender(value: String?): Gender? = value?.let { Gender.valueOf(it) }

    @TypeConverter
    fun fromReminderCategory(value: ReminderCategory): String = value.name

    @TypeConverter
    fun toReminderCategory(value: String): ReminderCategory = ReminderCategory.valueOf(value)

    @TypeConverter
    fun fromReminderType(value: ReminderType): String = value.name

    @TypeConverter
    fun toReminderType(value: String): ReminderType = ReminderType.valueOf(value)

    @TypeConverter
    fun fromReminderStatus(value: ReminderStatus): String = value.name

    @TypeConverter
    fun toReminderStatus(value: String): ReminderStatus = ReminderStatus.valueOf(value)

    @TypeConverter
    fun fromLedgerStatus(value: LedgerStatus): String = value.name

    @TypeConverter
    fun toLedgerStatus(value: String): LedgerStatus = LedgerStatus.valueOf(value)

    @TypeConverter
    fun fromItemType(value: ItemType): String = value.name

    @TypeConverter
    fun toItemType(value: String): ItemType = ItemType.valueOf(value)

    @TypeConverter
    fun fromEventType(value: EventType): String = value.name

    @TypeConverter
    fun toEventType(value: String): EventType = EventType.valueOf(value)
}