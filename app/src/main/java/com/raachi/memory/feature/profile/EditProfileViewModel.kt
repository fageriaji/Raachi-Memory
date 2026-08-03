package com.raachi.memory.feature.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.data.profile.ProfilePhotoStore
import com.raachi.memory.domain.model.Gender
import com.raachi.memory.domain.model.ProfileField
import com.raachi.memory.domain.model.ProfileInput
import com.raachi.memory.domain.model.ProfileValidationError
import com.raachi.memory.domain.usecase.ObserveProfileUseCase
import com.raachi.memory.domain.usecase.SaveProfileResult
import com.raachi.memory.domain.usecase.SaveProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val input: ProfileInput = ProfileInput(),
    val errors: Map<ProfileField, ProfileValidationError> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val saveFailed: Boolean = false,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    observeProfile: ObserveProfileUseCase,
    private val saveProfile: SaveProfileUseCase,
    private val photoStore: ProfilePhotoStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()
    private var initialPhotoUri: String? = null

    init {
        viewModelScope.launch {
            val profile = observeProfile().first()
            initialPhotoUri = profile?.profilePhotoUri
            _uiState.update {
                it.copy(
                    input = profile?.let { savedProfile ->
                        ProfileInput(
                            name = savedProfile.name,
                            dateOfBirth = savedProfile.dateOfBirth,
                            mobile = savedProfile.mobile.orEmpty(),
                            gender = savedProfile.gender,
                            email = savedProfile.email.orEmpty(),
                            heightCm = savedProfile.heightCm?.toDisplayValue().orEmpty(),
                            weightKg = savedProfile.weightKg?.toDisplayValue().orEmpty(),
                            profilePhotoUri = savedProfile.profilePhotoUri,
                        )
                    } ?: ProfileInput(),
                    isLoading = false,
                )
            }
        }
    }

    fun updateName(value: String) = updateInput(ProfileField.NAME) { copy(name = value) }

    fun updateDateOfBirth(value: LocalDate?) =
        updateInput(ProfileField.DATE_OF_BIRTH) { copy(dateOfBirth = value) }

    fun updateMobile(value: String) = updateInput(ProfileField.MOBILE) {
        copy(mobile = value.filter(Char::isDigit).take(MOBILE_LENGTH))
    }

    fun updateGender(value: Gender?) = updateInput { copy(gender = value) }

    fun updateEmail(value: String) = updateInput(ProfileField.EMAIL) { copy(email = value) }

    fun updateHeight(value: String) = updateInput(ProfileField.HEIGHT) { copy(heightCm = value) }

    fun updateWeight(value: String) = updateInput(ProfileField.WEIGHT) { copy(weightKg = value) }

    fun updateProfilePhoto(uri: String?) = updateInput { copy(profilePhotoUri = uri) }

    fun save() {
        if (_uiState.value.isSaving || _uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveFailed = false) }
            var persistedPhotoUri: String? = null
            try {
                val draftInput = _uiState.value.input
                val finalPhotoUri = when {
                    draftInput.profilePhotoUri == null -> null
                    draftInput.profilePhotoUri == initialPhotoUri -> initialPhotoUri
                    else -> photoStore.persist(Uri.parse(draftInput.profilePhotoUri)).also {
                        persistedPhotoUri = it
                    }
                }
                when (val result = saveProfile(draftInput.copy(profilePhotoUri = finalPhotoUri))) {
                    is SaveProfileResult.Invalid -> {
                        photoStore.delete(persistedPhotoUri)
                        _uiState.update {
                            it.copy(
                                errors = result.validation.errors,
                                isSaving = false,
                            )
                        }
                    }
                    is SaveProfileResult.Success -> {
                        if (initialPhotoUri != finalPhotoUri) photoStore.delete(initialPhotoUri)
                        initialPhotoUri = finalPhotoUri
                        _uiState.update {
                            it.copy(
                                input = it.input.copy(profilePhotoUri = finalPhotoUri),
                                isSaving = false,
                                isSaved = true,
                            )
                        }
                    }
                }
            } catch (_: Throwable) {
                photoStore.delete(persistedPhotoUri)
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

private fun Double.toDisplayValue(): String =
    if (this % 1.0 == 0.0) toInt().toString() else toString()
