package com.mesaitakibi.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Çalışan profili ve çalışma kuralları.
 *
 * @param grossWage aylık brüt ücret (MONTHLY) veya saatlik brüt ücret (HOURLY)
 * @param weeklyContractHours sözleşmedeki haftalık çalışma saati (yasal üst sınır 45)
 * @param monthlyHourDivisor aylık ücretten saatlik ücrete geçiş böleni (30 gün × 7,5 sa = 225)
 * @param nightStart / nightEnd gece çalışması penceresi (4857 sayılı Kanun: 20:00–06:00)
 * @param weeklyRestDay hafta tatili günü
 */
data class EmployeeProfile(
    val name: String = "",
    val wageType: WageType = WageType.MONTHLY,
    val grossWage: BigDecimal = BigDecimal.ZERO,
    val weeklyContractHours: Double = 45.0,
    val monthlyHourDivisor: Double = 225.0,
    val nightStart: LocalTime = LocalTime.of(20, 0),
    val nightEnd: LocalTime = LocalTime.of(6, 0),
    val weeklyRestDay: DayOfWeek = DayOfWeek.SUNDAY
) {
    /** Saatlik brüt ücret. */
    fun hourlyGross(): BigDecimal = when (wageType) {
        WageType.HOURLY -> grossWage
        WageType.MONTHLY -> grossWage.divide(BigDecimal(monthlyHourDivisor), 6, RoundingMode.HALF_UP)
    }
}
