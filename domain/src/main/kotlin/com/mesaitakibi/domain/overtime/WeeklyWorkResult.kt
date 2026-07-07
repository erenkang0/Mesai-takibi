package com.mesaitakibi.domain.overtime

import java.time.LocalDate

/** Bir günün çalışma dökümü. */
data class DayResult(
    val date: LocalDate,
    val workedMinutes: Long,
    val nightMinutes: Long,
    val isHoliday: Boolean,
    /** Plana göre fazladan çalışılan dakika (plan bazlı anlık mesai algılama). */
    val planOvertimeMinutes: Long
)

/**
 * Bir haftanın çalışma ve mesai analizi.
 * - [fazlaSureMinutes]: sözleşme saati ile 45 saat arası → %25 zamlı ücret
 * - [fazlaCalismaMinutes]: 45 saati aşan kısım (mesai) → %50 zamlı ücret
 */
data class WeeklyWorkResult(
    val weekStart: LocalDate,
    val totalWorkedMinutes: Long,
    val normalMinutes: Long,
    val fazlaSureMinutes: Long,
    val fazlaCalismaMinutes: Long,
    val nightMinutes: Long,
    val holidayMinutes: Long,
    val days: List<DayResult>
) {
    val hasOvertime: Boolean get() = fazlaCalismaMinutes > 0 || fazlaSureMinutes > 0
}
