package com.raachi.memory.domain.usecase

import com.raachi.memory.domain.model.ActivityEventType
import com.raachi.memory.domain.model.ActivityLog
import com.raachi.memory.domain.repository.ActivityRepository
import java.time.Clock
import javax.inject.Inject

class LogActivityUseCase @Inject constructor(
    private val repository: ActivityRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        eventType: ActivityEventType,
        referenceId: Long?,
        title: String,
        description: String? = null,
    ) {
        runCatching {
            repository.save(
                ActivityLog(
                    eventType = eventType,
                    referenceId = referenceId,
                    title = title,
                    description = description,
                    eventTime = clock.instant(),
                ),
            )
        }
    }
}
