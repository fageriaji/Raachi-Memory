package com.raachi.memory.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.model.UserProfile
import com.raachi.memory.domain.usecase.ObserveProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    data class Ready(val profile: UserProfile) : ProfileUiState

    data object Missing : ProfileUiState
}
@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeProfile: ObserveProfileUseCase,
) : ViewModel() {
    val uiState: StateFlow<ProfileUiState> = observeProfile()
        .map { profile ->
            profile?.let(ProfileUiState::Ready) ?: ProfileUiState.Missing
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState.Loading,
        )
}
