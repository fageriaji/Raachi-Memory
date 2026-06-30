package com.raachi.memory.domain.model

data class UserProfile(
    val id: Int = 1,
    val name: String,
    val gender: Gender?,
    val age: Int?,
    val birthday: Long?,
    val email: String?,
    val mobile: String?,
    val heightCm: Float?,
    val weightKg: Float?,
    val profilePhotoUri: String?,
    val createdAt: Long,
    val updatedAt: Long
)