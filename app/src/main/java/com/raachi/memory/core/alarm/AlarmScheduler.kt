package com.raachi.memory.core.alarm

import com.raachi.memory.domain.model.Reminder

interface AlarmScheduler {
    fun schedule(reminder: Reminder)
    fun cancel(reminderId: Int)
}