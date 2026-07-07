package com.mesaitakibi.data.repository

import com.mesaitakibi.data.local.dao.TransactionDao
import com.mesaitakibi.data.local.entity.TransactionEntity
import com.mesaitakibi.data.mapper.toDomain
import com.mesaitakibi.domain.finance.BudgetCalculator
import com.mesaitakibi.domain.finance.MonthlyBudget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepository @Inject constructor(
    private val dao: TransactionDao,
    private val budget: BudgetCalculator
) {
    fun observeAll(): Flow<List<TransactionEntity>> = dao.observeAll()

    suspend fun add(txn: TransactionEntity) = dao.insert(txn)

    suspend fun delete(txn: TransactionEntity) = dao.delete(txn)

    fun observeMonthlySummary(year: Int, month: Int): Flow<MonthlyBudget> =
        dao.observeAll().map { list ->
            budget.monthlySummary(year, month, list.map { it.toDomain() })
        }
}
