package com.raachi.memory.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.model.Gender
import com.raachi.memory.domain.model.ProfileField
import com.raachi.memory.domain.model.ProfileInput
import com.raachi.memory.domain.model.ProfileValidationError
import com.raachi.memory.domain.usecase.CompleteOnboardingUseCase
import com.raachi.memory.domain.usecase.SaveProfileResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val input: ProfileInput = ProfileInput(),
    val errors: Map<ProfileField, ProfileValidationError> = emptyMap(),
    val isSaving: Boolean = false,
    val isComplete: Boolean = false,
    val saveFailed: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateName(value: String) = updateInput(ProfileField.NAME) { copy(name = value) }

    fun updateDateOfBirth(value: LocalDate?) =
        updateInput(ProfileField.DATE_OF_BIRTH) { copy(dateOfBirth = value) }

    fun updateMobile(value: String) = updateInput(ProfileField.MOBILE) {
        copy(mobile = value.filter(Char::isDigit).take(MOBILE_LENGTH))
    }

    fun updateGender(value: Gender?) = updateInput { copy(gender = value) }

    fun updateEmail(value: String) = updateInput(ProfileField.EMAIL) { copy(email = value) }

    fun submit() {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveFailed = false) }
            runCatching { completeOnboarding(_uiState.value.input) }
                .onSuccess { result ->
                    when (result) {
                        is SaveProfileResult.Invalid -> _uiState.update {
                            it.copy(
                                errors = result.validation.errors,
                                isSaving = false,
                            )
                        }
                        is SaveProfileResult.Success -> _uiState.update {
                            it.copy(isSaving = false, isComplete = true)
                        }
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isSaving = false, saveFailed = true) }
                }
        }
    }

    private fun updateInput(
        field: ProfileField? = null,
        transform: ProfileInput.() -> ProfileInput,
    ) {
        _uiState.update { state ->
            state.copy(
                input = state.input.transform(),
                errors = field?.let(state.errors::minus) ?: state.errors,
                saveFailed = false,
            )
        }
    }

    private companion object {
        const val MOBILE_LENGTH = 10
    }
}
