package com.raachi.memory.feature.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.model.LedgerDirection
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.model.LedgerKind
import com.raachi.memory.domain.repository.LedgerRepository
import com.raachi.memory.domain.usecase.DeleteLedgerEntryUseCase
import com.raachi.memory.domain.usecase.MarkLedgerReturnedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LedgerTab { ALL, LENT, BORROWED }

data class LedgerSummary(
    val lentPaise: Long = 0,
    val borrowedPaise: Long = 0,
    val lentItems: Int = 0,
    val borrowedItems: Int = 0,
)

data class LedgerListUiState(
    val entries: List<LedgerEntry> = emptyList(),
    val summary: LedgerSummary = LedgerSummary(),
    val selectedTab: LedgerTab = LedgerTab.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
)

@HiltViewModel
class LedgerListViewModel @Inject constructor(
    repository: LedgerRepository,
    private val deleteEntry: DeleteLedgerEntryUseCase,
    private val markEntryReturned: MarkLedgerReturnedUseCase,
) : ViewModel() {
    private val selectedTab = MutableStateFlow(LedgerTab.ALL)
    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<LedgerListUiState> = combine(
        repository.observeAll(),
        selectedTab,
        searchQuery,
    ) { entries, tab, query ->
        val pending = entries.filterNot(LedgerEntry::isReturned)
        LedgerListUiState(
            entries = entries.filter { entry -> entry.matches(tab, query) },
            summary = LedgerSummary(
                lentPaise = pending.filter { it.direction == LedgerDirection.LENT }.sumOf { it.amountPaise ?: 0 },
                borrowedPaise = pending.filter { it.direction == LedgerDirection.BORROWED }.sumOf { it.amountPaise ?: 0 },
                lentItems = pending.count { it.direction == LedgerDirection.LENT && it.kind == LedgerKind.ITEM },
                borrowedItems = pending.count { it.direction == LedgerDirection.BORROWED && it.kind == LedgerKind.ITEM },
            ),
            selectedTab = tab,
            searchQuery = query,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LedgerListUiState(),
    )

    fun selectTab(tab: LedgerTab) { selectedTab.value = tab }
    fun updateSearch(query: String) { searchQuery.value = query }
    fun delete(id: Long) = viewModelScope.launch { deleteEntry(id) }
    fun markReturned(id: Long) = viewModelScope.launch { markEntryReturned(id) }
}

private fun LedgerEntry.matches(tab: LedgerTab, query: String): Boolean {
    val matchesTab = when (tab) {
        LedgerTab.ALL -> true
        LedgerTab.LENT -> direction == LedgerDirection.LENT
        LedgerTab.BORROWED -> direction == LedgerDirection.BORROWED
    }
    val trimmed = query.trim()
    return matchesTab && (
        trimmed.isBlank() || personName.contains(trimmed, ignoreCase = true) ||
            itemName?.contains(trimmed, ignoreCase = true) == true
        )
}
