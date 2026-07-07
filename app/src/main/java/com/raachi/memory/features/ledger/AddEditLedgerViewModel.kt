package com.raachi.memory.features.ledger

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.R
import com.raachi.memory.domain.model.ItemType
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.model.LedgerStatus
import com.raachi.memory.domain.repository.LedgerRepository
import com.raachi.memory.domain.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditLedgerUiState(
    val personName: String = "",
    val mobileNumber: String = "",
    val itemType: ItemType = ItemType.MONEY,
    val itemName: String = "",
    val amount: String = "",
    val borrowDateMillis: Long = DateTimeUtils.todayStartMillis(),
    val borrowHour: Int = 9,
    val borrowMinute: Int = 0,
    val dueDateMillis: Long = DateTimeUtils.todayStartMillis(),
    val dueHour: Int = 18,
    val dueMinute: Int = 0,
    val notes: String = "",
    val isLoading: Boolean = false
)

private data class LedgerValidationResult(
    val amount: Double?
)

@HiltViewModel
class AddEditLedgerViewModel @Inject constructor(
    private val ledgerRepository: LedgerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val ledgerId: Int = checkNotNull(savedStateHandle["ledgerId"])
    private var existingEntry: LedgerEntry? = null

    private val _uiState = MutableStateFlow(
        AddEditLedgerUiState(
            isLoading = ledgerId != NEW_LEDGER_ID
        )
    )
    val uiState: StateFlow<AddEditLedgerUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val messages: SharedFlow<Int> = _messages.asSharedFlow()

    private val _saveCompleted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val saveCompleted: SharedFlow<Unit> = _saveCompleted.asSharedFlow()

    init {
        if (ledgerId != NEW_LEDGER_ID) {
            loadLedgerEntry()
        }
    }

    fun updatePersonName(value: String) {
        updateState { copy(personName = value) }
    }

    fun updateMobileNumber(value: String) {
        updateState { copy(mobileNumber = value) }
    }

    fun updateItemType(value: ItemType) {
        updateState {
            copy(
                itemType = value,
                amount = if (value == ItemType.MONEY) amount else ""
            )
        }
    }

    fun updateItemName(value: String) {
        updateState { copy(itemName = value) }
    }

    fun updateAmount(value: String) {
        updateState { copy(amount = value) }
    }

    fun updateBorrowDate(value: Long) {
        updateState { copy(borrowDateMillis = value) }
    }

    fun updateBorrowTime(hour: Int, minute: Int) {
        updateState {
            copy(
                borrowHour = hour,
                borrowMinute = minute
            )
        }
    }

    fun updateDueDate(value: Long) {
        updateState { copy(dueDateMillis = value) }
    }

    fun updateDueTime(hour: Int, minute: Int) {
        updateState {
            copy(
                dueHour = hour,
                dueMinute = minute
            )
        }
    }

    fun updateNotes(value: String) {
        updateState { copy(notes = value) }
    }

    fun saveLedgerEntry() {
        viewModelScope.launch {
            val state = _uiState.value
            val validationResult = validate(state) ?: return@launch

            val borrowDateTime = DateTimeUtils.combineDateAndTime(
                dateMillis = state.borrowDateMillis,
                hour = state.borrowHour,
                minute = state.borrowMinute
            )

            val dueDateTime = DateTimeUtils.combineDateAndTime(
                dateMillis = state.dueDateMillis,
                hour = state.dueHour,
                minute = state.dueMinute
            )

            if (dueDateTime < borrowDateTime) {
                _messages.emit(R.string.ledger_error_due_before_borrow)
                return@launch
            }

            val now = System.currentTimeMillis()
            val previousEntry = existingEntry

            val ledgerEntry = LedgerEntry(
                id = previousEntry?.id ?: 0,
                personName = state.personName.trim(),
                mobileNumber = state.mobileNumber
                    .trim()
                    .takeIf { it.isNotBlank() },
                itemType = state.itemType,
                itemName = state.itemName.trim(),
                amount = validationResult.amount,
                borrowDateTime = borrowDateTime,
                dueDateTime = dueDateTime,
                status = previousEntry?.status ?: LedgerStatus.PENDING,
                returnedDateTime = previousEntry?.returnedDateTime,
                notes = state.notes.trim().takeIf { it.isNotBlank() },
                createdAt = previousEntry?.createdAt ?: now,
                updatedAt = now
            )

            try {
                if (previousEntry == null) {
                    ledgerRepository.insertEntry(ledgerEntry)
                    _messages.emit(R.string.ledger_saved_success)
                } else {
                    ledgerRepository.updateEntry(ledgerEntry)
                    _messages.emit(R.string.ledger_updated_success)
                }

                _saveCompleted.emit(Unit)
            } catch (_: Exception) {
                _messages.emit(R.string.ledger_error_save)
            }
        }
    }

    private fun loadLedgerEntry() {
        viewModelScope.launch {
            val entry = ledgerRepository.getEntryById(ledgerId).first()

            if (entry == null) {
                _messages.emit(R.string.ledger_error_not_found)
                updateState { copy(isLoading = false) }
                return@launch
            }

            existingEntry = entry

            val borrowZonedDateTime = java.time.Instant
                .ofEpochMilli(entry.borrowDateTime)
                .atZone(java.time.ZoneId.systemDefault())

            val dueDateTime = entry.dueDateTime ?: entry.borrowDateTime

            val dueZonedDateTime = java.time.Instant
                .ofEpochMilli(dueDateTime)
                .atZone(java.time.ZoneId.systemDefault())

            _uiState.value = AddEditLedgerUiState(
                personName = entry.personName,
                mobileNumber = entry.mobileNumber.orEmpty(),
                itemType = entry.itemType,
                itemName = entry.itemName.orEmpty(),
                amount = entry.amount?.toString().orEmpty(),
                borrowDateMillis = borrowZonedDateTime
                    .toLocalDate()
                    .atStartOfDay(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli(),
                borrowHour = borrowZonedDateTime.hour,
                borrowMinute = borrowZonedDateTime.minute,
                dueDateMillis = dueZonedDateTime
                    .toLocalDate()
                    .atStartOfDay(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli(),
                dueHour = dueZonedDateTime.hour,
                dueMinute = dueZonedDateTime.minute,
                notes = entry.notes.orEmpty(),
                isLoading = false
            )
        }
    }

    private suspend fun validate(
        state: AddEditLedgerUiState
    ): LedgerValidationResult? {
        if (state.personName.isBlank()) {
            _messages.emit(R.string.ledger_error_person_name_required)
            return null
        }

        if (state.itemName.isBlank()) {
            _messages.emit(R.string.ledger_error_item_name_required)
            return null
        }

        val mobileNumber = state.mobileNumber.trim()

        if (
            mobileNumber.isNotBlank() &&
            (
                    mobileNumber.length != MOBILE_NUMBER_LENGTH ||
                            mobileNumber.any { !it.isDigit() }
                    )
        ) {
            _messages.emit(R.string.ledger_error_mobile_invalid)
            return null
        }

        if (state.itemType != ItemType.MONEY) {
            return LedgerValidationResult(amount = null)
        }

        val amountText = state.amount.trim()

        if (amountText.isBlank()) {
            _messages.emit(R.string.ledger_error_amount_required)
            return null
        }

        val amount = amountText.toDoubleOrNull()

        if (amount == null || amount < 0.0) {
            _messages.emit(R.string.ledger_error_amount_invalid)
            return null
        }

        return LedgerValidationResult(amount = amount)
    }

    private fun updateState(
        transform: AddEditLedgerUiState.() -> AddEditLedgerUiState
    ) {
        _uiState.value = _uiState.value.transform()
    }

    private companion object {
        const val NEW_LEDGER_ID = -1
        const val MOBILE_NUMBER_LENGTH = 10
    }
}