package com.raachi.memory.feature.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raachi.memory.domain.model.ExpenseAccount
import com.raachi.memory.domain.model.ExpenseCategory
import com.raachi.memory.domain.model.ExpenseTransactionInput
import com.raachi.memory.domain.model.ExpenseTransactionType
import com.raachi.memory.domain.model.ExpenseTransactionValidation
import com.raachi.memory.domain.repository.ExpenseRepository
import com.raachi.memory.domain.usecase.DeleteExpenseTransactionUseCase
import com.raachi.memory.domain.usecase.SaveExpenseTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExpenseEditorUiState(
    val accounts: List<ExpenseAccount> = emptyList(),
    val input: ExpenseTransactionInput = ExpenseTransactionInput(),
    val validation: ExpenseTransactionValidation = ExpenseTransactionValidation(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false,
)

@HiltViewModel
class ExpenseEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ExpenseRepository,
    private val saveTransactionUseCase: SaveExpenseTransactionUseCase,
    private val deleteTransactionUseCase: DeleteExpenseTransactionUseCase,
) : ViewModel() {
    private val transactionId: Long? = savedStateHandle[TRANSACTION_ID_ARG]
    private val _uiState = MutableStateFlow(ExpenseEditorUiState())
    val uiState: StateFlow<ExpenseEditorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeActiveAccounts().collect { accounts ->
                val current = _uiState.value.input
                val existing = transactionId?.let { repository.getTransactionById(it) }
                val input = existing?.let { transaction ->
                    ExpenseTransactionInput(
                        id = transaction.id,
                        type = transaction.type,
                        amount = formatPaise(transaction.amountPaise),
                        sourceAccountId = transaction.sourceAccountId,
                        destinationAccountId = transaction.destinationAccountId,
                        category = transaction.category,
                        paymentMethod = transaction.paymentMethod,
                        transactionDate = transaction.transactionDate,
                        transactionTimeMinutes = transaction.transactionTimeMinutes,
                        note = transaction.note.orEmpty(),
                    )
                } ?: current.copy(
                    sourceAccountId = current.sourceAccountId ?: accounts.firstOrNull()?.id,
                    destinationAccountId = current.destinationAccountId ?: accounts.firstOrNull()?.id,
                )
                _uiState.update { it.copy(accounts = accounts, input = input, isLoading = false) }
            }
        }
    }

    fun updateInput(transform: ExpenseTransactionInput.() -> ExpenseTransactionInput) {
        _uiState.update { it.copy(input = it.input.transform(), validation = ExpenseTransactionValidation()) }
    }

    fun selectType(type: ExpenseTransactionType) {
        updateInput {
            copy(
                type = type,
                category = when (type) {
                    ExpenseTransactionType.DEBIT -> ExpenseCategory.FOOD
                    ExpenseTransactionType.CREDIT -> ExpenseCategory.SALARY
                    ExpenseTransactionType.TRANSFER -> ExpenseCategory.OTHER
                },
                paymentMethod = paymentMethod.takeUnless { type == ExpenseTransactionType.TRANSFER },
            )
        }
    }

    fun save() {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val validation = saveTransactionUseCase(_uiState.value.input)
            _uiState.update { it.copy(validation = validation, isSaving = false, saved = validation.isValid) }
        }
    }

    fun delete() {
        val id = transactionId ?: return
        viewModelScope.launch {
            deleteTransactionUseCase(id)
            _uiState.update { it.copy(deleted = true) }
        }
    }

    companion object { const val TRANSACTION_ID_ARG = "transactionId" }
}

private fun formatPaise(paise: Long): String = BigDecimal(paise).movePointLeft(2).stripTrailingZeros().toPlainString()
