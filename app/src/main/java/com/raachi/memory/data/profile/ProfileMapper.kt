package com.raachi.memory.data.profile

import com.raachi.memory.domain.model.Gender
import com.raachi.memory.domain.model.UserProfile
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    id = id,
    name = name,
    dateOfBirth = dateOfBirth?.let { storedDate ->
        runCatching { LocalDate.parse(storedDate, DATE_OF_BIRTH_FORMAT) }.getOrNull()
    },
    mobile = mobile,
    gender = gender?.let { stored -> Gender.entries.firstOrNull { it.name == stored } },
    email = email,
    heightCm = heightCm,
    weightKg = weightKg,
    profilePhotoUri = profilePhotoUri,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

internal fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    id = id,
    name = name,
    dateOfBirth = dateOfBirth?.format(DATE_OF_BIRTH_FORMAT),
    mobile = mobile,
    gender = gender?.name,
    email = email,
    heightCm = heightCm,
    weightKg = weightKg,
    profilePhotoUri = profilePhotoUri,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

private val DATE_OF_BIRTH_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu", Locale.ROOT)
