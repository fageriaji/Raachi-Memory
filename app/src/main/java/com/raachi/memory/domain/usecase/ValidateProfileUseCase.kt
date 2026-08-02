package com.raachi.memory.domain.usecase

import com.raachi.memory.domain.model.ProfileField
import com.raachi.memory.domain.model.ProfileInput
import com.raachi.memory.domain.model.ProfileValidationError
import com.raachi.memory.domain.model.ProfileValidationResult
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class ValidateProfileUseCase @Inject constructor(
    private val clock: Clock,
) {
    operator fun invoke(input: ProfileInput): ProfileValidationResult {
        val errors = buildMap {
            if (input.name.isBlank()) {
                put(ProfileField.NAME, ProfileValidationError.REQUIRED)
            }
            if (input.dateOfBirth?.isAfter(LocalDate.now(clock)) == true) {
                put(ProfileField.DATE_OF_BIRTH, ProfileValidationError.FUTURE_DATE)
            }
            if (input.mobile.isNotBlank() && !isValidMobile(input.mobile)) {
                put(ProfileField.MOBILE, ProfileValidationError.INVALID_MOBILE)
            }
            if (input.email.isNotBlank() && !isValidEmail(input.email)) {
                put(ProfileField.EMAIL, ProfileValidationError.INVALID_EMAIL)
            }
            input.heightCm.toOptionalDouble()?.let { height ->
                if (height !in MIN_HEIGHT_CM..MAX_HEIGHT_CM) {
                    put(ProfileField.HEIGHT, ProfileValidationError.INVALID_HEIGHT)
                }
            }
            if (input.heightCm.isNotBlank() && input.heightCm.toOptionalDouble() == null) {
                put(ProfileField.HEIGHT, ProfileValidationError.INVALID_HEIGHT)
            }
            input.weightKg.toOptionalDouble()?.let { weight ->
                if (weight !in MIN_WEIGHT_KG..MAX_WEIGHT_KG) {
                    put(ProfileField.WEIGHT, ProfileValidationError.INVALID_WEIGHT)
                }
            }
            if (input.weightKg.isNotBlank() && input.weightKg.toOptionalDouble() == null) {
                put(ProfileField.WEIGHT, ProfileValidationError.INVALID_WEIGHT)
            }
        }
        return ProfileValidationResult(errors)
    }

    private fun isValidMobile(value: String): Boolean = MOBILE_PATTERN.matches(value)

    private fun isValidEmail(value: String): Boolean {
        val normalized = value.trim().lowercase()
        if (!EMAIL_PATTERN.matches(normalized)) return false
        return normalized.substringAfterLast('@') in SUPPORTED_EMAIL_DOMAINS
    }

    private fun String.toOptionalDouble(): Double? = trim().takeIf(String::isNotEmpty)?.toDoubleOrNull()

    private companion object {
        val EMAIL_PATTERN = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        val MOBILE_PATTERN = Regex("^[5-9][0-9]{9}$")
        val SUPPORTED_EMAIL_DOMAINS = setOf(
            "aol.com",
            "fastmail.com",
            "gmail.com",
            "gmx.com",
            "gmx.net",
            "hey.com",
            "hotmail.com",
            "icloud.com",
            "live.com",
            "mail.com",
            "me.com",
            "msn.com",
            "outlook.com",
            "proton.me",
            "protonmail.com",
            "rediffmail.com",
            "yahoo.com",
            "ymail.com",
            "zoho.com",
        )
        const val MIN_HEIGHT_CM = 50.0
        const val MAX_HEIGHT_CM = 250.0
        const val MIN_WEIGHT_KG = 10.0
        const val MAX_WEIGHT_KG = 500.0
    }
}
