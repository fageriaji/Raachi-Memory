package com.raachi.memory.data.expense

import com.raachi.memory.domain.model.ExpenseAccount
import com.raachi.memory.domain.model.ExpenseTransaction
import com.raachi.memory.domain.repository.ExpenseRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineExpenseRepository @Inject constructor(private val dao: ExpenseDao) : ExpenseRepository {
    override fun observeActiveAccounts(): Flow<List<ExpenseAccount>> =
        dao.observeActiveAccounts().map { accounts -> accounts.map(ExpenseAccountEntity::toDomain) }

    override fun observeTransactions(): Flow<List<ExpenseTransaction>> =
        dao.observeTransactions().map { transactions -> transactions.map(ExpenseTransactionEntity::toDomain) }

    override suspend fun getAllAccounts() = dao.getAllAccounts().map(ExpenseAccountEntity::toDomain)
    override suspend fun getAllTransactions() = dao.getAllTransactions().map(ExpenseTransactionEntity::toDomain)
    override suspend fun getAccountById(id: Long) = dao.getAccountById(id)?.toDomain()
    override suspend fun getTransactionById(id: Long) = dao.getTransactionById(id)?.toDomain()
    override suspend fun saveAccount(account: ExpenseAccount) = dao.upsertAccount(account.toEntity())
    override suspend fun saveTransaction(transaction: ExpenseTransaction) = dao.upsertTransaction(transaction.toEntity())
    override suspend fun deleteTransaction(id: Long) = dao.deleteTransactionById(id)
}
