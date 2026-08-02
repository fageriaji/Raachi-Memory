package com.raachi.memory.data.profile

import com.raachi.memory.domain.model.UserProfile
import com.raachi.memory.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineProfileRepository @Inject constructor(
    private val dao: UserProfileDao,
) : ProfileRepository {
    override fun observeProfile(): Flow<UserProfile?> = dao.observeProfile().map { it?.toDomain() }

    override suspend fun getProfile(): UserProfile? = dao.getProfile()?.toDomain()

    override suspend fun saveProfile(profile: UserProfile) {
        dao.upsert(profile.toEntity())
    }
}
