package com.raachi.memory.domain.repository

import com.raachi.memory.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun insertUserProfile(userProfile: UserProfile)
}