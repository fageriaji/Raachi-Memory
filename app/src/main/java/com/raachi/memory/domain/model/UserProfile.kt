package com.raachi.memory.domain.model

import java.time.LocalDate

data class UserProfile(
    val id: Long = SINGLE_USER_ID,
    val name: String,
    val dateOfBirth: LocalDate? = null,
    val mobile: String? = null,
    val gender: Gender? = null,
    val email: String? = null,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val profilePhotoUri: String? = null,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
) {
    val bmi: Double?
        get() {
            val height = heightCm ?: return null
            val weight = weightKg ?: return null
            if (height <= 0.0 || weight <= 0.0) return null
            val heightMetres = height / 100.0
            return weight / (heightMetres * heightMetres)
        }

    companion object {
        const val SINGLE_USER_ID = 1L
    }
}
