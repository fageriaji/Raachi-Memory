package com.raachi.memory.domain.model

import java.time.LocalDate

data class ProfileInput(
    val name: String = "",
    val dateOfBirth: LocalDate? = null,
    val mobile: String = "",
    val gender: Gender? = null,
    val email: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val profilePhotoUri: String? = null,
)

enum class ProfileField {
    NAME,
    DATE_OF_BIRTH,
    MOBILE,
    EMAIL,
    HEIGHT,
    WEIGHT,
}
enum class ProfileValidationError {
    REQUIRED,
    FUTURE_DATE,
    INVALID_MOBILE,
    INVALID_EMAIL,
    INVALID_HEIGHT,
    INVALID_WEIGHT,
}

data class ProfileValidationResult(
    val errors: Map<ProfileField, ProfileValidationError>,
) {
    val isValid: Boolean = errors.isEmpty()
}
