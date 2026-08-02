package com.raachi.memory.data.expense

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expense_accounts WHERE isArchived = 0 ORDER BY type, createdAtMillis, id")
    fun observeActiveAccounts(): Flow<List<ExpenseAccountEntity>>

    @Query("SELECT * FROM expense_accounts ORDER BY id")
    suspend fun getAllAccounts(): List<ExpenseAccountEntity>

    @Query("SELECT * FROM expense_accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): ExpenseAccountEntity?

    @Upsert
    suspend fun upsertAccount(account: ExpenseAccountEntity): Long

    @Upsert
    suspend fun upsertAccounts(accounts: List<ExpenseAccountEntity>)

    @Query("SELECT * FROM expense_transactions ORDER BY transactionDateEpochDay DESC, transactionTimeMinutes DESC, createdAtMillis DESC")
    fun observeTransactions(): Flow<List<ExpenseTransactionEntity>>

    @Query("SELECT * FROM expense_transactions ORDER BY id")
    suspend fun getAllTransactions(): List<ExpenseTransactionEntity>

    @Query("SELECT * FROM expense_transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): ExpenseTransactionEntity?

    @Upsert
    suspend fun upsertTransaction(transaction: ExpenseTransactionEntity): Long

    @Upsert
    suspend fun upsertTransactions(transactions: List<ExpenseTransactionEntity>)

    @Query("DELETE FROM expense_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM expense_transactions")
    suspend fun deleteAllTransactions()

    @Query("DELETE FROM expense_accounts")
    suspend fun deleteAllAccounts()
}
