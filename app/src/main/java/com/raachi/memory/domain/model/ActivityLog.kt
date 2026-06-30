package com.raachi.memory.domain.model

data class ActivityLog(
    val id: Int = 0,
    val eventType: EventType,
    val referenceId: Int,
    val title: String,
    val description: String?,
    val eventTime: Long
)