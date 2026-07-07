package com.mesaitakibi.domain.payroll

import com.mesaitakibi.domain.model.EmployeeProfile
import com.mesaitakibi.domain.model.WageType
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PayrollCalculatorTest {

    private val calc = PayrollCalculator()
    private val tax = TaxParameters.default2026()

    private fun assertMoney(expected: String, actual: BigDecimal, msg: String = "") {
        assertEquals(0, BigDecimal(expected).compareTo(actual), "$msg (beklenen=$expected, gerçek=$actual)")
    }

    @Test
    fun `asgari ucret brutten nete 28075,50 verir`() {
        val profile = EmployeeProfile(
            wageType = WageType.MONTHLY,
            grossWage = BigDecimal("33030.00")
        )
        val work = MonthlyWork(year = 2026, monthIndex = 1)

        val r = calc.calculate(work, profile, tax)

        assertMoney("33030.00", r.gross, "brüt")
        assertMoney("4624.20", r.sgkEmployee, "SGK işçi")
        assertMoney("330.30", r.unemployment, "işsizlik")
        assertMoney("0.00", r.incomeTax, "gelir vergisi (istisna)")
        assertMoney("0.00", r.stampTax, "damga vergisi (istisna)")
        assertMoney("28075.50", r.net, "net")
    }

    @Test
    fun `50000 brut maasta kesintiler ve net dogru`() {
        val profile = EmployeeProfile(
            wageType = WageType.MONTHLY,
            grossWage = BigDecimal("50000.00")
        )
        val work = MonthlyWork(year = 2026, monthIndex = 1)

        val r = calc.calculate(work, profile, tax)

        assertMoney("50000.00", r.gross, "brüt")
        assertMoney("7000.00", r.sgkEmployee, "SGK işçi %14")
        assertMoney("500.00", r.unemployment, "işsizlik %1")
        // Gelir vergisi: matrah 42500 → %15 = 6375; asgari ücret istisnası 4211,325 → 2163,68
        assertMoney("2163.68", r.incomeTax, "gelir vergisi (istisna sonrası)")
        // Damga: 379,50 − 250,70 (istisna) = 128,80
        assertMoney("128.80", r.stampTax, "damga vergisi (istisna sonrası)")
        assertMoney("9792.48", r.totalDeductions, "toplam kesinti")
        assertMoney("40207.52", r.net, "net")
    }

    @Test
    fun `mesai zamlari bruite eklenir`() {
        val profile = EmployeeProfile(
            wageType = WageType.MONTHLY,
            grossWage = BigDecimal("33030.00"),
            monthlyHourDivisor = 225.0
        )
        // saatlik = 33030 / 225 = 146,80
        val work = MonthlyWork(
            year = 2026,
            monthIndex = 1,
            fazlaCalismaHours = BigDecimal("10"), // 10 saat mesai ×1,50
            fazlaSureHours = BigDecimal("5")      // 5 saat fazla süre ×1,25
        )

        val r = calc.calculate(work, profile, tax)

        // fazla çalışma = 10 × 146,80 × 1,50 = 2202,00
        assertMoney("2202.00", r.fazlaCalismaPay, "fazla çalışma ücreti")
        // fazla süre = 5 × 146,80 × 1,25 = 917,50
        assertMoney("917.50", r.fazlaSurePay, "fazla süre ücreti")
        // brüt = 33030 + 2202 + 917,50 = 36149,50
        assertMoney("36149.50", r.gross, "mesaili brüt")
        assertTrue(r.net > BigDecimal("28075.50"), "mesaili net asgari üstü olmalı")
    }

    @Test
    fun `istisna kapaliyken asgari ucrette gelir ve damga vergisi kesilir`() {
        val noExemption = tax.copy(applyMinimumWageExemption = false)
        val profile = EmployeeProfile(wageType = WageType.MONTHLY, grossWage = BigDecimal("33030.00"))
        val work = MonthlyWork(year = 2026, monthIndex = 1)

        val r = calc.calculate(work, profile, noExemption)

        assertTrue(r.incomeTax > BigDecimal.ZERO, "istisna kapalı → gelir vergisi > 0")
        assertTrue(r.stampTax > BigDecimal.ZERO, "istisna kapalı → damga vergisi > 0")
        assertTrue(r.net < BigDecimal("28075.50"), "istisna kapalı → net daha düşük")
    }
}
