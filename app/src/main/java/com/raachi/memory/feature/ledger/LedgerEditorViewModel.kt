package com.raachi.memory.feature.ledger

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.model.LedgerInput
import com.raachi.memory.domain.model.LedgerValidation
import com.raachi.memory.domain.repository.LedgerRepository
import com.raachi.memory.domain.usecase.DeleteLedgerEntryUseCase
import com.raachi.memory.domain.usecase.SaveLedgerEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LedgerEditorUiState(
    val input: LedgerInput = LedgerInput(),
    val validation: LedgerValidation = LedgerValidation(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false,
)

@HiltViewModel
class LedgerEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LedgerRepository,
    private val saveEntry: SaveLedgerEntryUseCase,
    private val deleteEntry: DeleteLedgerEntryUseCase,
) : ViewModel() {
    private val entryId: Long? = savedStateHandle[LEDGER_ID_ARG]
    private val _uiState = MutableStateFlow(LedgerEditorUiState(isLoading = entryId != null))
    val uiState: StateFlow<LedgerEditorUiState> = _uiState.asStateFlow()

    init {
        entryId?.let { id ->
            viewModelScope.launch {
                val entry = repository.getById(id)
                _uiState.update { state ->
                    state.copy(
                        input = entry?.let {
                            LedgerInput(
                                id = it.id,
                                personName = it.personName,
                                mobileNumber = it.mobileNumber.orEmpty(),
                                kind = it.kind,
                                direction = it.direction,
                                itemName = it.itemName.orEmpty(),
                                amount = it.amountPaise?.let(::formatPaise).orEmpty(),
                                transactionDate = it.transactionDate,
                                dueDate = it.dueDate,
                                notes = it.notes.orEmpty(),
                            )
                        } ?: state.input,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun updateInput(transform: LedgerInput.() -> LedgerInput) {
        _uiState.update { it.copy(input = it.input.transform(), validation = LedgerValidation()) }
    }

    fun save() {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val validation = saveEntry(_uiState.value.input)
            _uiState.update { it.copy(validation = validation, isSaving = false, saved = validation.isValid) }
        }
    }

    fun delete() {
        val id = entryId ?: return
        viewModelScope.launch {
            deleteEntry(id)
            _uiState.update { it.copy(deleted = true) }
        }
    }

    companion object { const val LEDGER_ID_ARG = "ledgerId" }
}

private fun formatPaise(paise: Long): String = BigDecimal(paise).movePointLeft(2).stripTrailingZeros().toPlainString()
