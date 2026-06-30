package com.raachi.memory.features.profile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashState {
    data object Loading : SplashState
    data object GoToDashboard : SplashState
    data object GoToWelcome : SplashState
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashState>(SplashState.Loading)
    val uiState: StateFlow<SplashState> = _uiState.asStateFlow()

    init {
        checkFirstLaunchState()
    }

    private fun checkFirstLaunchState() {
        viewModelScope.launch {
            try {
                val settings = settingsRepository.getAppSettings().first()

                Log.d(
                    "RaachiDebug",
                    "First Launch Completed: ${settings.firstLaunchCompleted}"
                )

                if (settings.firstLaunchCompleted) {
                    _uiState.value = SplashState.GoToDashboard
                } else {
                    _uiState.value = SplashState.GoToWelcome
                }
            } catch (e: Exception) {
                Log.e("RaachiDebug", "Error reading AppSettings", e)
                _uiState.value = SplashState.GoToWelcome
            }
        }
    }
}