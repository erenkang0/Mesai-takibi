package com.mesaitakibi.domain.payroll

import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SeveranceCalculatorTest {

    private val calc = SeveranceCalculator()
    private val ceiling = BigDecimal("100000") // tavan (yüksek → sınırlamaz)

    @Test
    fun `1 yildan az calismada kidem tazminati hakki yok`() {
        val r = calc.calculate(
            serviceStart = LocalDate.of(2025, 6, 1),
            serviceEnd = LocalDate.of(2026, 1, 1),
            monthlyGross = BigDecimal("40000"),
            severanceCeiling = ceiling
        )
        assertFalse(r.eligibleForSeverance)
        assertEquals(0, BigDecimal.ZERO.compareTo(r.severanceGross))
        assertEquals(4, r.noticeWeeks) // 6 ay–1,5 yıl
    }

    @Test
    fun `6 yil calismada kidem hakki ve 8 hafta ihbar`() {
        val r = calc.calculate(
            serviceStart = LocalDate.of(2020, 1, 1),
            serviceEnd = LocalDate.of(2026, 1, 1),
            monthlyGross = BigDecimal("50000"),
            severanceCeiling = ceiling
        )
        assertTrue(r.eligibleForSeverance)
        assertEquals(8, r.noticeWeeks) // 3 yıl+
        // ~6 yıl × 50000 → 300.000 civarı
        assertTrue(r.severanceGross > BigDecimal("295000"))
        assertTrue(r.severanceGross < BigDecimal("305000"))
        // net = brüt − damga
        assertEquals(0, (r.severanceGross - r.severanceStampTax).compareTo(r.severanceNet))
        assertTrue(r.severanceStampTax > BigDecimal.ZERO)
    }

    @Test
    fun `tavan kidem tazminatini sinirlar`() {
        val lowCeiling = BigDecimal("30000")
        val r = calc.calculate(
            serviceStart = LocalDate.of(2024, 1, 1),
            serviceEnd = LocalDate.of(2026, 1, 1), // ~2 yıl
            monthlyGross = BigDecimal("80000"),
            severanceCeiling = lowCeiling
        )
        // 2 yıl × 30000 (tavan) ≈ 60.000; 80000 kullanılsaydı ~160.000 olurdu
        assertTrue(r.severanceGross < BigDecimal("62000"))
        assertEquals(6, r.noticeWeeks) // 1,5–3 yıl
    }

    @Test
    fun `ihbar suresi kidemle artar`() {
        fun weeks(start: LocalDate) = calc.calculate(
            start, LocalDate.of(2026, 1, 1), BigDecimal("40000"), ceiling
        ).noticeWeeks
        assertEquals(2, weeks(LocalDate.of(2025, 11, 1)))  // <6 ay
        assertEquals(4, weeks(LocalDate.of(2025, 1, 1)))   // ~1 yıl
        assertEquals(6, weeks(LocalDate.of(2024, 1, 1)))   // ~2 yıl
        assertEquals(8, weeks(LocalDate.of(2021, 1, 1)))   // ~5 yıl
    }
}
