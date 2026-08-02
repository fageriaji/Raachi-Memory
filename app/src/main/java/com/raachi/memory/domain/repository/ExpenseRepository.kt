package com.raachi.memory.domain.repository

import com.raachi.memory.domain.model.ExpenseAccount
import com.raachi.memory.domain.model.ExpenseTransaction
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeActiveAccounts(): Flow<List<ExpenseAccount>>
    fun observeTransactions(): Flow<List<ExpenseTransaction>>
    suspend fun getAllAccounts(): List<ExpenseAccount>
    suspend fun getAllTransactions(): List<ExpenseTransaction>
    suspend fun getAccountById(id: Long): ExpenseAccount?
    suspend fun getTransactionById(id: Long): ExpenseTransaction?
    suspend fun saveAccount(account: ExpenseAccount): Long
    suspend fun saveTransaction(transaction: ExpenseTransaction): Long
    suspend fun deleteTransaction(id: Long)
}
