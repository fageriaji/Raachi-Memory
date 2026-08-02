package com.raachi.memory.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.model.ActivityLog
import com.raachi.memory.domain.model.ActivitySource
import com.raachi.memory.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class ActivityFilter { ALL, REMINDERS, LEDGER }

data class ActivityUiState(
    val activities: List<ActivityLog> = emptyList(),
    val selectedFilter: ActivityFilter = ActivityFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    repository: ActivityRepository,
) : ViewModel() {
    private val selectedFilter = MutableStateFlow(ActivityFilter.ALL)
    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<ActivityUiState> = combine(
        repository.observeAll(),
        selectedFilter,
        searchQuery,
    ) { activities, filter, query ->
        ActivityUiState(
            activities = filterActivityLogs(activities, filter, query),
            selectedFilter = filter,
            searchQuery = query,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ActivityUiState(),
    )

    fun selectFilter(filter: ActivityFilter) {
        selectedFilter.value = filter
    }

    fun updateSearch(query: String) {
        searchQuery.value = query
    }
}

internal fun filterActivityLogs(
    activities: List<ActivityLog>,
    filter: ActivityFilter,
    query: String,
): List<ActivityLog> {
    val trimmedQuery = query.trim()
    return activities.filter { activity ->
        val matchesFilter = when (filter) {
            ActivityFilter.ALL -> true
            ActivityFilter.REMINDERS -> activity.source == ActivitySource.REMINDER
            ActivityFilter.LEDGER -> activity.source == ActivitySource.LEDGER
        }
        matchesFilter && (
            trimmedQuery.isBlank() ||
                activity.title.contains(trimmedQuery, ignoreCase = true) ||
                activity.description?.contains(trimmedQuery, ignoreCase = true) == true
            )
    }
}
