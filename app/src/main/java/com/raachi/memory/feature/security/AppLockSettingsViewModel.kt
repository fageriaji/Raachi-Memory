package com.raachi.memory.feature.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.model.AppLockSettings
import com.raachi.memory.domain.security.AppLockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppLockSettingsMessage {
    ENABLED,
    PASSCODE_CHANGED,
    INCORRECT_PASSCODE,
    PASSCODES_DO_NOT_MATCH,
    DISABLED,
}

data class AppLockSettingsUiState(
    val settings: AppLockSettings = AppLockSettings(),
    val working: Boolean = false,
    val message: AppLockSettingsMessage? = null,
)

@HiltViewModel
class AppLockSettingsViewModel @Inject constructor(
    private val manager: AppLockManager,
) : ViewModel() {
    private val operation = MutableStateFlow(OperationState())
    val uiState: StateFlow<AppLockSettingsUiState> = combine(manager.settings, operation) { settings, operation ->
        AppLockSettingsUiState(settings, operation.working, operation.message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLockSettingsUiState())

    fun generateRecoveryCode(): String = manager.generateRecoveryCode()

    fun enable(passcode: String, confirmation: String, recoveryCode: String, biometricEnabled: Boolean) {
        if (!validPair(passcode, confirmation)) return
        viewModelScope.launch {
            operation.value = OperationState(working = true)
            manager.enable(passcode, recoveryCode, biometricEnabled)
            operation.value = OperationState(message = AppLockSettingsMessage.ENABLED)
        }
    }

    fun changePasscode(current: String, newPasscode: String, confirmation: String) {
        if (!validPair(newPasscode, confirmation)) return
        viewModelScope.launch {
            operation.value = OperationState(working = true)
            operation.value = if (manager.changePasscode(current, newPasscode)) {
                OperationState(message = AppLockSettingsMessage.PASSCODE_CHANGED)
            } else {
                OperationState(message = AppLockSettingsMessage.INCORRECT_PASSCODE)
            }
        }
    }

    fun regenerateRecoveryCode(currentPasscode: String, onGenerated: (String) -> Unit) {
        viewModelScope.launch {
            operation.value = OperationState(working = true)
            val recoveryCode = manager.regenerateRecoveryCode(currentPasscode)
            if (recoveryCode == null) {
                operation.value = OperationState(message = AppLockSettingsMessage.INCORRECT_PASSCODE)
            } else {
                operation.value = OperationState()
                onGenerated(recoveryCode)
            }
        }
    }

    fun disable(passcode: String) {
        viewModelScope.launch {
            operation.value = OperationState(working = true)
            operation.value = if (manager.disable(passcode)) {
                OperationState(message = AppLockSettingsMessage.DISABLED)
            } else {
                OperationState(message = AppLockSettingsMessage.INCORRECT_PASSCODE)
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) = viewModelScope.launch {
        manager.setBiometricEnabled(enabled)
    }

    fun consumeMessage() {
        operation.value = operation.value.copy(message = null)
    }

    private fun validPair(passcode: String, confirmation: String): Boolean {
        if (passcode.length != 6 || !passcode.all(Char::isDigit)) {
            operation.value = OperationState(message = AppLockSettingsMessage.INCORRECT_PASSCODE)
            return false
        }
        if (passcode != confirmation) {
            operation.value = OperationState(message = AppLockSettingsMessage.PASSCODES_DO_NOT_MATCH)
            return false
        }
        return true
    }

    private data class OperationState(
        val working: Boolean = false,
        val message: AppLockSettingsMessage? = null,
    )
}
