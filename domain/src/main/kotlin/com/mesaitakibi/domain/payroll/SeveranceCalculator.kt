package com.mesaitakibi.domain.payroll

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Kıdem ve ihbar tazminatı sonucu. */
data class SeveranceResult(
    val serviceDays: Long,
    val serviceYears: BigDecimal,
    val eligibleForSeverance: Boolean, // kıdem tazminatı en az 1 yıl için
    val severanceGross: BigDecimal,
    val severanceStampTax: BigDecimal,
    val severanceNet: BigDecimal,
    val noticeWeeks: Int,
    val noticePayGross: BigDecimal
)

/**
 * Kıdem ve ihbar tazminatı hesaplayıcı (4857 sayılı İş Kanunu, 1475/14).
 *
 * - Kıdem tazminatı: her tam yıl için 30 günlük brüt ücret (kesirli yıllar oranlanır),
 *   yıllık tavan ile sınırlı. Gelir vergisinden istisna; yalnızca damga vergisi kesilir.
 * - İhbar süresi kıdeme göre: <6 ay 2, 6 ay–1,5 yıl 4, 1,5–3 yıl 6, 3 yıl+ 8 hafta.
 *
 * Not: Kıdem tazminatı tavanı 6 ayda bir güncellenir; parametre olarak verilir.
 */
class SeveranceCalculator {
    private val rm = RoundingMode.HALF_UP

    fun calculate(
        serviceStart: LocalDate,
        serviceEnd: LocalDate,
        monthlyGross: BigDecimal,
        severanceCeiling: BigDecimal,
        stampTaxRate: BigDecimal = BigDecimal("0.00759")
    ): SeveranceResult {
        val days = ChronoUnit.DAYS.between(serviceStart, serviceEnd).coerceAtLeast(0)
        val years = BigDecimal(days).divide(BigDecimal(365), 6, rm)
        val eligible = days >= 365

        val cappedMonthly = monthlyGross.min(severanceCeiling)
        val severanceGross = if (eligible) (cappedMonthly * years).setScale(2, rm) else BigDecimal.ZERO.setScale(2)
        val stamp = (severanceGross * stampTaxRate).setScale(2, rm)
        val net = (severanceGross - stamp).setScale(2, rm)

        val weeks = when {
            days < 182 -> 2
            days < 547 -> 4
            days < 1095 -> 6
            else -> 8
        }
        val dailyGross = monthlyGross.divide(BigDecimal(30), 6, rm)
        val noticePayGross = (dailyGross * BigDecimal(weeks * 7)).setScale(2, rm)

        return SeveranceResult(
            serviceDays = days,
            serviceYears = years.setScale(2, rm),
            eligibleForSeverance = eligible,
            severanceGross = severanceGross,
            severanceStampTax = stamp,
            severanceNet = net,
            noticeWeeks = weeks,
            noticePayGross = noticePayGross
        )
    }
}
