package com.mesaitakibi.domain.payroll

import com.mesaitakibi.domain.model.EmployeeProfile
import com.mesaitakibi.domain.model.WageType
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PayrollAdjustmentTest {

    private val calc = PayrollCalculator()
    private val tax = TaxParameters.default2026()
    private val profile = EmployeeProfile(wageType = WageType.MONTHLY, grossWage = BigDecimal("50000.00"))
    private val work = MonthlyWork(year = 2026, monthIndex = 1)

    private fun money(v: BigDecimal) = v

    @Test
    fun `net ek kalemleri net maasi etkiler`() {
        val base = calc.calculate(work, profile, tax)
        val withAdj = calc.calculate(
            work, profile, tax,
            netAdditions = BigDecimal("1500"),   // yol/yemek (istisna)
            netDeductions = BigDecimal("2000")   // avans kesintisi
        )
        // net = baz net + 1500 − 2000 = baz net − 500
        assertEquals(0, (base.net - BigDecimal("500")).compareTo(withAdj.net))
        assertEquals(0, BigDecimal("1500").compareTo(withAdj.netAdditions))
        assertEquals(0, BigDecimal("2000").compareTo(withAdj.netDeductions))
    }

    @Test
    fun `vergiye tabi ek odeme brute eklenir ve kesinti dogurur`() {
        val base = calc.calculate(work, profile, tax)
        val withPrim = calc.calculate(
            work, profile, tax,
            additionalTaxableEarnings = BigDecimal("10000") // prim (vergiye tabi)
        )
        assertEquals(0, (base.gross + BigDecimal("10000")).compareTo(withPrim.gross))
        // prim vergiye tabi olduğundan kesintiler artar → net artışı 10000'den az
        val netIncrease = withPrim.net - base.net
        assertTrue(netIncrease > BigDecimal.ZERO)
        assertTrue(netIncrease < BigDecimal("10000"))
    }
}
