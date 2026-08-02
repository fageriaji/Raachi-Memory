package com.raachi.memory.feature.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.model.Reminder
import com.raachi.memory.domain.model.ReminderStatus
import com.raachi.memory.domain.repository.ReminderRepository
import com.raachi.memory.domain.usecase.CompleteReminderUseCase
import com.raachi.memory.domain.usecase.DeleteReminderUseCase
import com.raachi.memory.domain.usecase.SkipReminderUseCase
import com.raachi.memory.domain.usecase.SnoozeReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ReminderTab { ACTIVE, SNOOZED, COMPLETED }

data class ReminderListUiState(
    val reminders: List<Reminder> = emptyList(),
    val selectedTab: ReminderTab = ReminderTab.ACTIVE,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
)

@HiltViewModel
class ReminderListViewModel @Inject constructor(
    repository: ReminderRepository,
    private val snoozeReminder: SnoozeReminderUseCase,
    private val completeReminder: CompleteReminderUseCase,
    private val skipReminder: SkipReminderUseCase,
    private val deleteReminder: DeleteReminderUseCase,
) : ViewModel() {
    private val selectedTab = MutableStateFlow(ReminderTab.ACTIVE)
    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<ReminderListUiState> = combine(
        repository.observeAll(),
        selectedTab,
        searchQuery,
    ) { reminders, tab, query ->
        ReminderListUiState(
            reminders = reminders.filter { it.matches(tab, query) },
            selectedTab = tab,
            searchQuery = query,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReminderListUiState(),
    )

    fun selectTab(tab: ReminderTab) {
        selectedTab.value = tab
    }

    fun updateSearch(query: String) {
        searchQuery.value = query
    }

    fun snooze(id: Long) = viewModelScope.launch { snoozeReminder(id) }

    fun complete(id: Long) = viewModelScope.launch { completeReminder(id) }

    fun skip(id: Long) = viewModelScope.launch { skipReminder(id) }

    fun delete(id: Long) = viewModelScope.launch { deleteReminder(id) }
}

private fun Reminder.matches(tab: ReminderTab, query: String): Boolean {
    val matchesTab = when (tab) {
        ReminderTab.ACTIVE -> status == ReminderStatus.ACTIVE
        ReminderTab.SNOOZED -> status == ReminderStatus.SNOOZED
        ReminderTab.COMPLETED -> status in setOf(ReminderStatus.COMPLETED, ReminderStatus.SKIPPED)
    }
    return matchesTab && (query.isBlank() || title.contains(query.trim(), ignoreCase = true))
}
