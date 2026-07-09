package com.raachi.memory.features.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.R
import com.raachi.memory.core.ledgeralarm.LedgerAlarmScheduler
import com.raachi.memory.core.ledgeralarm.LedgerNotificationHelper
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.model.LedgerStatus
import com.raachi.memory.domain.repository.LedgerRepository
import com.raachi.memory.domain.util.isOverdue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LedgerFilter {
    ALL,
    PENDING,
    RETURNED,
    OVERDUE
}

enum class LedgerSort {
    DUE_EARLIEST,
    DUE_LATEST,
    PERSON_NAME,
    RECENTLY_ADDED
}

data class LedgerUiState(
    val entries: List<LedgerEntry> = emptyList(),
    val searchQuery: String = "",
    val filter: LedgerFilter = LedgerFilter.ALL,
    val sort: LedgerSort = LedgerSort.DUE_EARLIEST,
    val currentTimeMillis: Long = System.currentTimeMillis()
)

private data class LedgerControls(
    val searchQuery: String,
    val filter: LedgerFilter,
    val sort: LedgerSort
)

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val ledgerRepository: LedgerRepository,
    private val ledgerAlarmScheduler: LedgerAlarmScheduler,
    private val ledgerNotificationHelper: LedgerNotificationHelper
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow(LedgerFilter.ALL)
    private val selectedSort = MutableStateFlow(LedgerSort.DUE_EARLIEST)

    private val _messages = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val messages: SharedFlow<Int> = _messages.asSharedFlow()

    private val controls: Flow<LedgerControls> = combine(
        searchQuery,
        selectedFilter,
        selectedSort
    ) { query, filter, sort ->
        LedgerControls(
            searchQuery = query,
            filter = filter,
            sort = sort
        )
    }

    private val currentTimeMillis: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(CLOCK_REFRESH_INTERVAL_MILLIS)
        }
    }

    val uiState: StateFlow<LedgerUiState> = combine(
        ledgerRepository.getAllEntries(),
        controls,
        currentTimeMillis
    ) { entries, controls, now ->
        val visibleEntries = entries
            .filter { entry ->
                entry.matchesSearch(controls.searchQuery) &&
                        entry.matchesFilter(controls.filter, now)
            }
            .sortedWith(entryComparator(controls.sort))

        LedgerUiState(
            entries = visibleEntries,
            searchQuery = controls.searchQuery,
            filter = controls.filter,
            sort = controls.sort,
            currentTimeMillis = now
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = LedgerUiState()
    )

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun selectFilter(filter: LedgerFilter) {
        selectedFilter.value = filter
    }

    fun selectSort(sort: LedgerSort) {
        selectedSort.value = sort
    }

    fun markAsReturned(entry: LedgerEntry) {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()

                ledgerRepository.updateEntry(
                    entry.copy(
                        status = LedgerStatus.RETURNED,
                        returnedDateTime = now,
                        snoozedUntil = null,
                        updatedAt = now
                    )
                )

                ledgerAlarmScheduler.cancel(entry.id)
                ledgerNotificationHelper.cancelNotification(entry.id)

                _messages.emit(R.string.ledger_returned_success)
            } catch (_: Exception) {
                _messages.emit(R.string.ledger_error_mark_returned)
            }
        }
    }

    fun deleteEntry(entry: LedgerEntry) {
        viewModelScope.launch {
            try {
                ledgerRepository.deleteEntry(entry)
                ledgerAlarmScheduler.cancel(entry.id)
                ledgerNotificationHelper.cancelNotification(entry.id)
                _messages.emit(R.string.ledger_deleted_success)
            } catch (_: Exception) {
                _messages.emit(R.string.ledger_error_delete)
            }
        }
    }

    private fun LedgerEntry.matchesSearch(query: String): Boolean {
        if (query.isBlank()) {
            return true
        }

        val normalizedQuery = query.trim()

        return personName.contains(normalizedQuery, ignoreCase = true) ||
                mobileNumber.orEmpty().contains(normalizedQuery, ignoreCase = true) ||
                itemName.orEmpty().contains(normalizedQuery, ignoreCase = true)
    }

    private fun LedgerEntry.matchesFilter(
        filter: LedgerFilter,
        now: Long
    ): Boolean {
        return when (filter) {
            LedgerFilter.ALL -> true
            LedgerFilter.PENDING -> status == LedgerStatus.PENDING
            LedgerFilter.RETURNED -> status == LedgerStatus.RETURNED
            LedgerFilter.OVERDUE -> isOverdue(now)
        }
    }

    private fun entryComparator(sort: LedgerSort): Comparator<LedgerEntry> {
        return when (sort) {
            LedgerSort.DUE_EARLIEST -> compareBy<LedgerEntry> {
                it.dueDateTime == null
            }.thenBy {
                it.dueDateTime ?: Long.MAX_VALUE
            }

            LedgerSort.DUE_LATEST -> compareBy<LedgerEntry> {
                it.dueDateTime == null
            }.thenByDescending {
                it.dueDateTime ?: Long.MIN_VALUE
            }

            LedgerSort.PERSON_NAME -> compareBy(
                String.CASE_INSENSITIVE_ORDER
            ) { entry ->
                entry.personName
            }

            LedgerSort.RECENTLY_ADDED -> compareByDescending<LedgerEntry> {
                it.createdAt
            }
        }
    }

    private companion object {
        const val CLOCK_REFRESH_INTERVAL_MILLIS = 60_000L
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}