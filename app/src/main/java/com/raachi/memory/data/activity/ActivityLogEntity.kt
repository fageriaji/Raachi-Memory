package com.raachi.memory.data.activity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_logs",
    indices = [Index(value = ["eventTimeMillis"]), Index(value = ["eventType"])],
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,
    val referenceId: Long?,
    val title: String,
    val description: String?,
    val eventTimeMillis: Long,
)
