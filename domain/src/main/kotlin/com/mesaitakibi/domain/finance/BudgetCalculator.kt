package com.mesaitakibi.domain.finance

import java.math.BigDecimal

enum class TransactionType { INCOME, EXPENSE }

/** Kişisel finans işlemi (gelir/gider). Net maaş otomatik gelir olarak eklenebilir. */
data class FinanceTransaction(
    val amount: BigDecimal,
    val type: TransactionType,
    val category: String,
    val year: Int,
    val month: Int
)

/** Aylık bütçe özeti. */
data class MonthlyBudget(
    val year: Int,
    val month: Int,
    val totalIncome: BigDecimal,
    val totalExpense: BigDecimal,
    val balance: BigDecimal,
    val expenseByCategory: Map<String, BigDecimal>
)

/** Kişisel finans/bütçe motoru: işlemleri ay ve kategoriye göre toplar. */
class BudgetCalculator {
    fun monthlySummary(year: Int, month: Int, transactions: List<FinanceTransaction>): MonthlyBudget {
        val inMonth = transactions.filter { it.year == year && it.month == month }
        val income = inMonth.filter { it.type == TransactionType.INCOME }.sumAmount()
        val expense = inMonth.filter { it.type == TransactionType.EXPENSE }.sumAmount()
        val byCategory = inMonth
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumAmount() }
        return MonthlyBudget(
            year = year,
            month = month,
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense,
            expenseByCategory = byCategory
        )
    }

    private fun List<FinanceTransaction>.sumAmount(): BigDecimal =
        fold(BigDecimal.ZERO) { acc, t -> acc + t.amount }
}
