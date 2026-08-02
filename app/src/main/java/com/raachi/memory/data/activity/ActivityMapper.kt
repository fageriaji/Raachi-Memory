package com.raachi.memory.data.activity

import com.raachi.memory.domain.model.ActivityEventType
import com.raachi.memory.domain.model.ActivityLog
import java.time.Instant

fun ActivityLogEntity.toDomain(): ActivityLog = ActivityLog(
    id = id,
    eventType = ActivityEventType.valueOf(eventType),
    referenceId = referenceId,
    title = title,
    description = description,
    eventTime = Instant.ofEpochMilli(eventTimeMillis),
)

fun ActivityLog.toEntity(): ActivityLogEntity = ActivityLogEntity(
    id = id,
    eventType = eventType.name,
    referenceId = referenceId,
    title = title,
    description = description,
    eventTimeMillis = eventTime.toEpochMilli(),
)
