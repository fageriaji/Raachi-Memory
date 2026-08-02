package com.raachi.memory.feature.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.data.backup.BackupManager
import com.raachi.memory.domain.model.AppPreferences
import com.raachi.memory.domain.model.ThemeMode
import com.raachi.memory.domain.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SettingsMessage { EXPORT_COMPLETE, IMPORT_COMPLETE, OPERATION_FAILED }

data class SettingsUiState(
    val preferences: AppPreferences = AppPreferences(),
    val isWorking: Boolean = false,
    val message: SettingsMessage? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AppSettingsRepository,
    private val backupManager: BackupManager,
) : ViewModel() {
    private val operation = MutableStateFlow(OperationState())

    val uiState: StateFlow<SettingsUiState> = combine(repository.preferences, operation) { preferences, state ->
        SettingsUiState(preferences, state.isWorking, state.message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repository.setThemeMode(mode) }

    fun setReminderSound(enabled: Boolean) = viewModelScope.launch {
        repository.setReminderSoundEnabled(enabled)
    }

    fun setDefaultSnooze(minutes: Int) = viewModelScope.launch {
        repository.setDefaultSnoozeMinutes(minutes)
    }

    fun export(uri: Uri) = runOperation(SettingsMessage.EXPORT_COMPLETE) { backupManager.exportTo(uri) }

    fun import(uri: Uri) = runOperation(SettingsMessage.IMPORT_COMPLETE) { backupManager.importFrom(uri) }

    fun consumeMessage() {
        operation.value = operation.value.copy(message = null)
    }

    private fun runOperation(success: SettingsMessage, block: suspend () -> Unit) = viewModelScope.launch {
        operation.value = OperationState(isWorking = true)
        operation.value = try {
            block()
            OperationState(message = success)
        } catch (_: Exception) {
            OperationState(message = SettingsMessage.OPERATION_FAILED)
        }
    }
}

private data class OperationState(
    val isWorking: Boolean = false,
    val message: SettingsMessage? = null,
)
