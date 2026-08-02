package com.raachi.memory.data.profile

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val dateOfBirth: String?,
    val mobile: String?,
    val gender: String?,
    val email: String?,
    val heightCm: Double?,
    val weightKg: Double?,
    val profilePhotoUri: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
