package com.raachi.memory.feature.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.os.SystemClock
import com.raachi.memory.domain.model.AppLockSettings
import com.raachi.memory.domain.security.AppLockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppLockError { INVALID_PASSCODE, INVALID_RECOVERY_CODE, PASSCODES_DO_NOT_MATCH }

data class AppLockGateUiState(
    val loading: Boolean = true,
    val settings: AppLockSettings = AppLockSettings(),
    val unlocked: Boolean = false,
    val working: Boolean = false,
    val error: AppLockError? = null,
    val lockoutSeconds: Int = 0,
)

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val manager: AppLockManager,
) : ViewModel() {
    private var resetAuthorizationDeadline = 0L
    private val settingsState = manager.settings
        .map<AppLockSettings, AppLockSettings?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val operation = MutableStateFlow(OperationState())

    val uiState: StateFlow<AppLockGateUiState> = combine(
        settingsState,
        manager.unlocked,
        operation,
    ) { settings, unlocked, operation ->
        AppLockGateUiState(
            loading = settings == null,
            settings = settings ?: AppLockSettings(),
            unlocked = unlocked,
            working = operation.working,
            error = operation.error,
            lockoutSeconds = operation.lockoutSeconds,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLockGateUiState())

    fun unlockWithPasscode(passcode: String) = verifyCredential {
        manager.verifyPasscode(passcode)
    }

    fun authorizeRecoveryCode(code: String, onAuthorized: () -> Unit) = verifyCredential(
        invalidError = AppLockError.INVALID_RECOVERY_CODE,
        onSuccess = {
            authorizeReset()
            onAuthorized()
        },
    ) {
        manager.verifyRecoveryCode(code)
    }

    fun authorizeWithDeviceCredential(onAuthorized: () -> Unit) {
        authorizeReset()
        onAuthorized()
    }

    fun resetPasscode(passcode: String, confirmation: String, onReset: (String) -> Unit) {
        if (SystemClock.elapsedRealtime() > resetAuthorizationDeadline) {
            operation.value = operation.value.copy(error = AppLockError.INVALID_RECOVERY_CODE)
            return
        }
        if (passcode.length != 6 || !passcode.all(Char::isDigit)) {
            operation.value = operation.value.copy(error = AppLockError.INVALID_PASSCODE)
            return
        }
        if (passcode != confirmation) {
            operation.value = operation.value.copy(error = AppLockError.PASSCODES_DO_NOT_MATCH)
            return
        }
        viewModelScope.launch {
            operation.value = operation.value.copy(working = true, error = null)
            val recoveryCode = manager.resetPasscode(passcode)
            resetAuthorizationDeadline = 0L
            operation.value = OperationState(lockoutLevel = operation.value.lockoutLevel)
            onReset(recoveryCode)
        }
    }

    fun unlockAfterReset() {
        operation.value = OperationState()
        manager.unlockSession()
    }

    fun onBiometricAuthenticated() {
        operation.value = OperationState()
        manager.unlockSession()
    }

    fun lock() {
        resetAuthorizationDeadline = 0L
        manager.lockSession()
    }

    fun clearError() {
        operation.value = operation.value.copy(error = null)
    }

    private fun verifyCredential(
        invalidError: AppLockError = AppLockError.INVALID_PASSCODE,
        onSuccess: () -> Unit = manager::unlockSession,
        verify: suspend () -> Boolean,
    ) {
        if (operation.value.working || operation.value.lockoutSeconds > 0) return
        viewModelScope.launch {
            operation.value = operation.value.copy(working = true, error = null)
            if (verify()) {
                operation.value = OperationState()
                onSuccess()
            } else {
                val failedAttempts = operation.value.failedAttempts + 1
                val lockoutLevel = operation.value.lockoutLevel
                if (failedAttempts >= ATTEMPTS_PER_LOCKOUT) {
                    startLockout(lockoutLevel)
                } else {
                    operation.value = operation.value.copy(
                        working = false,
                        error = invalidError,
                        failedAttempts = failedAttempts,
                    )
                }
            }
        }
    }

    private fun authorizeReset() {
        resetAuthorizationDeadline = SystemClock.elapsedRealtime() + RESET_AUTHORIZATION_MILLIS
    }

    private suspend fun startLockout(currentLevel: Int) {
        val duration = LOCKOUT_DURATIONS_SECONDS[currentLevel.coerceAtMost(LOCKOUT_DURATIONS_SECONDS.lastIndex)]
        for (remaining in duration downTo 1) {
            operation.value = OperationState(
                error = AppLockError.INVALID_PASSCODE,
                lockoutSeconds = remaining,
                lockoutLevel = currentLevel + 1,
            )
            delay(1_000)
        }
        operation.value = OperationState(lockoutLevel = currentLevel + 1)
    }

    private data class OperationState(
        val working: Boolean = false,
        val error: AppLockError? = null,
        val failedAttempts: Int = 0,
        val lockoutSeconds: Int = 0,
        val lockoutLevel: Int = 0,
    )

    private companion object {
        const val ATTEMPTS_PER_LOCKOUT = 5
        const val RESET_AUTHORIZATION_MILLIS = 120_000L
        val LOCKOUT_DURATIONS_SECONDS = listOf(30, 60, 300)
    }
}
