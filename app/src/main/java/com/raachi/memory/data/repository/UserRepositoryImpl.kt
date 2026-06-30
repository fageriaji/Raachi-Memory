package com.raachi.memory.data.repository

import com.raachi.memory.data.local.dao.UserDao
import com.raachi.memory.domain.model.UserProfile
import com.raachi.memory.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun getUserProfile(): Flow<UserProfile?> {
        return userDao.getUserProfile().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun insertUserProfile(userProfile: UserProfile) {
        userDao.insertUserProfile(
            com.raachi.memory.data.local.entity.UserProfileEntity.fromDomain(userProfile)
        )
    }
}