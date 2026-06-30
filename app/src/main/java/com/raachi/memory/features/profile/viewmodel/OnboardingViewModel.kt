package com.raachi.memory.features.profile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.model.Gender
import com.raachi.memory.domain.model.UserProfile
import com.raachi.memory.domain.model.AppSettings
import com.raachi.memory.domain.repository.SettingsRepository
import com.raachi.memory.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve name passed from NameInputScreen via Navigation Argument
    private val name: String = checkNotNull(savedStateHandle["name"])

    var email by mutableStateOf("")
    var mobile by mutableStateOf("")
    var height by mutableStateOf("")
    var weight by mutableStateOf("")
    var gender by mutableStateOf<Gender?>(null)

    private val _onboardingCompleted = MutableSharedFlow<Unit>()
    val onboardingCompleted: SharedFlow<Unit> = _onboardingCompleted.asSharedFlow()

    fun saveProfileAndComplete() {
        viewModelScope.launch {
            val userProfile = UserProfile(
                id = 1, // Single user app
                name = name.trim(),
                email = email.trim().takeIf { it.isNotBlank() },
                mobile = mobile.trim().takeIf { it.isNotBlank() },
                heightCm = height.toFloatOrNull(),
                weightKg = weight.toFloatOrNull(),
                gender = gender,
                age = null, // Will be calculated from birthday in future phases
                birthday = null,
                profilePhotoUri = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Persist the user
            userRepository.insertUserProfile(userProfile)

            // Mark onboarding as complete in DataStore/Settings
            val currentSettings = settingsRepository.getAppSettings().firstOrNull() ?: AppSettings()
            settingsRepository.updateAppSettings(currentSettings.copy(firstLaunchCompleted = true))

            _onboardingCompleted.emit(Unit)
        }
    }
}