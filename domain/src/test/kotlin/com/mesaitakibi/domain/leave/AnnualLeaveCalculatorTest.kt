package com.mesaitakibi.domain.leave

import kotlin.test.Test
import kotlin.test.assertEquals

class AnnualLeaveCalculatorTest {

    private val calc = AnnualLeaveCalculator()

    @Test
    fun `kidem araliklarina gore izin gunu`() {
        assertEquals(0, calc.entitlementDays(serviceYears = 0, age = 30))
        assertEquals(14, calc.entitlementDays(serviceYears = 3, age = 30))
        assertEquals(14, calc.entitlementDays(serviceYears = 5, age = 30))
        assertEquals(20, calc.entitlementDays(serviceYears = 10, age = 30))
        assertEquals(26, calc.entitlementDays(serviceYears = 15, age = 30))
        assertEquals(26, calc.entitlementDays(serviceYears = 25, age = 30))
    }

    @Test
    fun `50 yas ustu ve 18 yas alti en az 20 gun`() {
        assertEquals(20, calc.entitlementDays(serviceYears = 3, age = 55)) // 14 yerine 20
        assertEquals(20, calc.entitlementDays(serviceYears = 2, age = 17)) // 14 yerine 20
        assertEquals(26, calc.entitlementDays(serviceYears = 20, age = 60)) // zaten 26
    }
}
