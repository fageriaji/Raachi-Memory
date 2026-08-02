package com.raachi.memory.domain.repository

import com.raachi.memory.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile?>

    suspend fun getProfile(): UserProfile?

    suspend fun saveProfile(profile: UserProfile)
}
