package com.raachi.memory.domain.usecase

import com.raachi.memory.domain.model.UserProfile
import com.raachi.memory.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    operator fun invoke(): Flow<UserProfile?> = repository.observeProfile()
}
