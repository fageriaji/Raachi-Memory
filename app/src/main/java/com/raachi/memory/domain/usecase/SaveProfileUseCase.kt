package com.raachi.memory.domain.usecase

import com.raachi.memory.domain.model.ProfileInput
import com.raachi.memory.domain.model.ProfileValidationResult
import com.raachi.memory.domain.model.UserProfile
import com.raachi.memory.domain.repository.ProfileRepository
import java.time.Clock
import javax.inject.Inject

class SaveProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val validateProfile: ValidateProfileUseCase,
    private val clock: Clock,
) {
    suspend operator fun invoke(input: ProfileInput): SaveProfileResult {
        val validation = validateProfile(input)
        if (!validation.isValid) return SaveProfileResult.Invalid(validation)

        val existing = profileRepository.getProfile()
        val now = clock.millis()
        val profile = UserProfile(
            id = existing?.id ?: UserProfile.SINGLE_USER_ID,
            name = input.name.trim(),
            dateOfBirth = input.dateOfBirth,
            mobile = input.mobile.trim().ifEmpty { null },
            gender = input.gender,
            email = input.email.trim().ifEmpty { null },
            heightCm = input.heightCm.trim().toDoubleOrNull(),
            weightKg = input.weightKg.trim().toDoubleOrNull(),
            profilePhotoUri = input.profilePhotoUri,
            createdAtMillis = existing?.createdAtMillis ?: now,
            updatedAtMillis = now,
        )
        profileRepository.saveProfile(profile)
        return SaveProfileResult.Success(profile)
    }
}
sealed interface SaveProfileResult {
    data class Success(val profile: UserProfile) : SaveProfileResult

    data class Invalid(val validation: ProfileValidationResult) : SaveProfileResult
}
