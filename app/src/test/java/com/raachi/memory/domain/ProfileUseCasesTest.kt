package com.raachi.memory.domain

import com.raachi.memory.domain.model.ProfileField
import com.raachi.memory.domain.model.ProfileInput
import com.raachi.memory.domain.model.ProfileValidationError
import com.raachi.memory.domain.model.UserProfile
import com.raachi.memory.domain.repository.AppSettingsRepository
import com.raachi.memory.domain.model.AppPreferences
import com.raachi.memory.domain.model.ThemeMode
import com.raachi.memory.domain.repository.ProfileRepository
import com.raachi.memory.domain.usecase.CompleteOnboardingUseCase
import com.raachi.memory.domain.usecase.SaveProfileResult
import com.raachi.memory.domain.usecase.SaveProfileUseCase
import com.raachi.memory.domain.usecase.ValidateProfileUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileUseCasesTest {
    private val clock = Clock.fixed(Instant.parse("2026-07-29T10:00:00Z"), ZoneOffset.UTC)
    private val validator = ValidateProfileUseCase(clock)

    @Test
    fun blankName_isRejected() {
        val result = validator(ProfileInput(name = " "))

        assertEquals(ProfileValidationError.REQUIRED, result.errors[ProfileField.NAME])
        assertFalse(result.isValid)
    }

    @Test
    fun optionalFieldsCanRemainBlank() {
        val result = validator(ProfileInput(name = "Mannu"))

        assertTrue(result.isValid)
    }

    @Test
    fun mobileMustBeTenDigitsAndStartFromFiveToNine() {
        listOf("5123456789", "6123456789", "7123456789", "8123456789", "9123456789")
            .forEach { mobile ->
                assertTrue(validator(ProfileInput(name = "Mannu", mobile = mobile)).isValid)
            }

        listOf("4123456789", "912345678", "91234567890", "+919123456789")
            .forEach { mobile ->
                val result = validator(ProfileInput(name = "Mannu", mobile = mobile))
                assertEquals(
                    ProfileValidationError.INVALID_MOBILE,
                    result.errors[ProfileField.MOBILE],
                )
            }
    }

    @Test
    fun emailRequiresSupportedProvider() {
        listOf(
            "user@gmail.com",
            "user@outlook.com",
            "user@yahoo.com",
            "user@ymail.com",
            "user@live.com",
            "user@icloud.com",
        ).forEach { email ->
            assertTrue(validator(ProfileInput(name = "Mannu", email = email)).isValid)
        }

        val result = validator(ProfileInput(name = "Mannu", email = "user@example.com"))
        assertEquals(ProfileValidationError.INVALID_EMAIL, result.errors[ProfileField.EMAIL])
    }

    @Test
    fun invalidOptionalValues_areReportedTogether() {
        val result = validator(
            ProfileInput(
                name = "Mannu",
                dateOfBirth = LocalDate.of(2027, 1, 1),
                mobile = "123",
                email = "not-an-email",
                heightCm = "20",
                weightKg = "900",
            ),
        )

        assertEquals(ProfileValidationError.FUTURE_DATE, result.errors[ProfileField.DATE_OF_BIRTH])
        assertEquals(ProfileValidationError.INVALID_MOBILE, result.errors[ProfileField.MOBILE])
        assertEquals(ProfileValidationError.INVALID_EMAIL, result.errors[ProfileField.EMAIL])
        assertEquals(ProfileValidationError.INVALID_HEIGHT, result.errors[ProfileField.HEIGHT])
        assertEquals(ProfileValidationError.INVALID_WEIGHT, result.errors[ProfileField.WEIGHT])
    }

    @Test
    fun validOnboarding_savesProfileBeforeCompletingOnboarding() = runTest {
        val profiles = FakeProfileRepository()
        val settings = FakeSettingsRepository()
        val saveProfile = SaveProfileUseCase(profiles, validator, clock)
        val completeOnboarding = CompleteOnboardingUseCase(saveProfile, settings)

        val result = completeOnboarding(
            ProfileInput(
                name = " Mannu ",
                email = " mannu@gmail.com ",
                profilePhotoUri = "file:///profile-photo.jpg",
            ),
        )

        assertTrue(result is SaveProfileResult.Success)
        assertEquals("Mannu", profiles.profile.value?.name)
        assertEquals("mannu@gmail.com", profiles.profile.value?.email)
        assertEquals("file:///profile-photo.jpg", profiles.profile.value?.profilePhotoUri)
        assertTrue(settings.preferencesState.value.onboardingCompleted)
    }

    @Test
    fun invalidOnboarding_doesNotPersistOrComplete() = runTest {
        val profiles = FakeProfileRepository()
        val settings = FakeSettingsRepository()
        val saveProfile = SaveProfileUseCase(profiles, validator, clock)
        val completeOnboarding = CompleteOnboardingUseCase(saveProfile, settings)

        val result = completeOnboarding(ProfileInput())

        assertTrue(result is SaveProfileResult.Invalid)
        assertNull(profiles.profile.value)
        assertFalse(settings.preferencesState.value.onboardingCompleted)
    }

    @Test
    fun bmi_isCalculatedFromHeightAndWeight() {
        val profile = UserProfile(
            name = "Mannu",
            heightCm = 180.0,
            weightKg = 81.0,
            createdAtMillis = clock.millis(),
            updatedAtMillis = clock.millis(),
        )

        assertEquals(25.0, profile.bmi ?: 0.0, 0.01)
    }
}

private class FakeProfileRepository : ProfileRepository {
    val profile = MutableStateFlow<UserProfile?>(null)

    override fun observeProfile(): Flow<UserProfile?> = profile

    override suspend fun getProfile(): UserProfile? = profile.value

    override suspend fun saveProfile(profile: UserProfile) {
        this.profile.value = profile
    }
}

private class FakeSettingsRepository : AppSettingsRepository {
    val preferencesState = MutableStateFlow(AppPreferences())

    override val preferences: Flow<AppPreferences> = preferencesState
    override val onboardingCompleted: Flow<Boolean> = preferencesState.map { it.onboardingCompleted }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        preferencesState.value = preferencesState.value.copy(onboardingCompleted = completed)
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        preferencesState.value = preferencesState.value.copy(themeMode = mode)
    }

    override suspend fun setReminderSoundEnabled(enabled: Boolean) {
        preferencesState.value = preferencesState.value.copy(reminderSoundEnabled = enabled)
    }

    override suspend fun setDefaultSnoozeMinutes(minutes: Int) {
        preferencesState.value = preferencesState.value.copy(defaultSnoozeMinutes = minutes)
    }

    override suspend fun replacePreferences(preferences: AppPreferences) {
        preferencesState.value = preferences
    }
}
