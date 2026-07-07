package com.mesaitakibi.domain.overtime

import kotlin.test.Test
import kotlin.test.assertEquals

class AnnualOvertimeTest {

    @Test
    fun `yillik mesai seviyeleri`() {
        assertEquals(OvertimeLevel.NORMAL, AnnualOvertimeTracker.status(100.0).level)   // %37
        assertEquals(OvertimeLevel.WARNING, AnnualOvertimeTracker.status(210.0).level)  // %77
        assertEquals(OvertimeLevel.CRITICAL, AnnualOvertimeTracker.status(250.0).level) // %92
        assertEquals(OvertimeLevel.EXCEEDED, AnnualOvertimeTracker.status(280.0).level) // >270
    }

    @Test
    fun `kalan saat dogru`() {
        val s = AnnualOvertimeTracker.status(200.0)
        assertEquals(70.0, s.remainingHours, 0.001)
        assertEquals(270, s.limitHours)
    }
}
