package com.mesaitakibi.domain.finance

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class BudgetCalculatorTest {

    private val calc = BudgetCalculator()

    @Test
    fun `aylik ozet gelir gider ve kategori dagilimini dogru hesaplar`() {
        val txns = listOf(
            FinanceTransaction(BigDecimal("28075.50"), TransactionType.INCOME, "Maaş", 2026, 1),
            FinanceTransaction(BigDecimal("5000"), TransactionType.EXPENSE, "Kira", 2026, 1),
            FinanceTransaction(BigDecimal("1500"), TransactionType.EXPENSE, "Market", 2026, 1),
            FinanceTransaction(BigDecimal("500"), TransactionType.EXPENSE, "Market", 2026, 1),
            FinanceTransaction(BigDecimal("9999"), TransactionType.EXPENSE, "Kira", 2026, 2) // başka ay
        )

        val b = calc.monthlySummary(2026, 1, txns)

        assertEquals(0, BigDecimal("28075.50").compareTo(b.totalIncome))
        assertEquals(0, BigDecimal("7000").compareTo(b.totalExpense))
        assertEquals(0, BigDecimal("21075.50").compareTo(b.balance))
        assertEquals(0, BigDecimal("2000").compareTo(b.expenseByCategory.getValue("Market")))
        assertEquals(0, BigDecimal("5000").compareTo(b.expenseByCategory.getValue("Kira")))
    }
}
