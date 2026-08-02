package com.raachi.memory.feature.reminder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.model.ReminderInput
import com.raachi.memory.domain.model.ReminderValidation
import com.raachi.memory.domain.repository.ReminderRepository
import com.raachi.memory.domain.usecase.DeleteReminderUseCase
import com.raachi.memory.domain.usecase.SaveReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReminderEditorUiState(
    val input: ReminderInput,
    val validation: ReminderValidation = ReminderValidation(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false,
)

@HiltViewModel
class ReminderEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ReminderRepository,
    private val saveReminder: SaveReminderUseCase,
    private val deleteReminder: DeleteReminderUseCase,
    private val clock: Clock,
) : ViewModel() {
    private val reminderId: Long? = savedStateHandle[REMINDER_ID_ARG]
    private val _uiState = MutableStateFlow(
        ReminderEditorUiState(
            input = ReminderInput(startDate = java.time.LocalDate.now(clock)),
            isLoading = reminderId != null,
        ),
    )
    val uiState: StateFlow<ReminderEditorUiState> = _uiState.asStateFlow()

    init {
        reminderId?.let { id ->
            viewModelScope.launch {
                val reminder = repository.getById(id)
                _uiState.update { state ->
                    state.copy(
                        input = reminder?.let {
                            val scheduled = (it.nextTriggerAt ?: it.scheduledAt).atZone(clock.zone)
                            ReminderInput(
                                id = it.id,
                                title = it.title,
                                category = it.category,
                                description = it.description.orEmpty(),
                                repeatType = it.repeatType,
                                intervalHours = it.intervalHours ?: 2,
                                startDate = scheduled.toLocalDate(),
                                startTime = scheduled.toLocalTime().withSecond(0).withNano(0),
                                soundEnabled = it.soundEnabled,
                                vibrationEnabled = it.vibrationEnabled,
                            )
                        } ?: state.input,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun updateInput(transform: ReminderInput.() -> ReminderInput) {
        _uiState.update { it.copy(input = it.input.transform(), validation = ReminderValidation()) }
    }

    fun save() {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val validation = saveReminder(_uiState.value.input)
            _uiState.update {
                it.copy(
                    validation = validation,
                    isSaving = false,
                    saved = validation.isValid,
                )
            }
        }
    }

    fun delete() {
        val id = reminderId ?: return
        viewModelScope.launch {
            deleteReminder(id)
            _uiState.update { it.copy(deleted = true) }
        }
    }

    companion object {
        const val REMINDER_ID_ARG = "reminderId"
    }
}
