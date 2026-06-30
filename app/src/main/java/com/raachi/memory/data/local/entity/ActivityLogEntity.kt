package com.raachi.memory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.raachi.memory.domain.model.ActivityLog
import com.raachi.memory.domain.model.EventType

@Entity(
    tableName = "activity_logs",
    indices = [Index(value = ["event_time"])]
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "event_type") val eventType: EventType,
    @ColumnInfo(name = "reference_id") val referenceId: Int,
    val title: String,
    val description: String?,
    @ColumnInfo(name = "event_time") val eventTime: Long
) {
    fun toDomain() = ActivityLog(id, eventType, referenceId, title, description, eventTime)

    companion object {
        fun fromDomain(model: ActivityLog) = ActivityLogEntity(model.id, model.eventType, model.referenceId, model.title, model.description, model.eventTime)
    }
}