package com.raachi.memory.domain.usecase

import com.raachi.memory.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveOnboardingCompletionUseCase @Inject constructor(
    private val repository: AppSettingsRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.onboardingCompleted
}
