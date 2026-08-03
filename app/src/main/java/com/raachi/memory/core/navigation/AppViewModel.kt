package com.raachi.memory.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.usecase.ObserveOnboardingCompletionUseCase
import com.raachi.memory.domain.usecase.ObserveProfileUseCase
import com.raachi.memory.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface AppStartState {
    data object Loading : AppStartState

    data object NeedsOnboarding : AppStartState

    data object Ready : AppStartState
}

@HiltViewModel
class AppViewModel @Inject constructor(
    observeOnboardingCompletion: ObserveOnboardingCompletionUseCase,
    observeProfile: ObserveProfileUseCase,
) : ViewModel() {
    val startState: StateFlow<AppStartState> = observeOnboardingCompletion()
        .map { completed ->
            if (completed) AppStartState.Ready else AppStartState.NeedsOnboarding
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppStartState.Loading,
        )

    val profile: StateFlow<UserProfile?> = observeProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
}
