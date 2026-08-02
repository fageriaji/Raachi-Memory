package com.raachi.memory.domain.usecase

import com.raachi.memory.domain.model.ProfileInput
import com.raachi.memory.domain.repository.AppSettingsRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val saveProfile: SaveProfileUseCase,
    private val settingsRepository: AppSettingsRepository,
) {
    suspend operator fun invoke(input: ProfileInput): SaveProfileResult {
        val result = saveProfile(input)
        if (result is SaveProfileResult.Success) {
            settingsRepository.setOnboardingCompleted(true)
        }
        return result
    }
}
