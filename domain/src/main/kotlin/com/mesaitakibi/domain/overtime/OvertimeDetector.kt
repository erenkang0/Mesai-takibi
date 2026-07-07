package com.mesaitakibi.domain.overtime

import com.mesaitakibi.domain.model.EmployeeProfile
import com.mesaitakibi.domain.model.PlannedShift
import com.mesaitakibi.domain.model.WorkSession
import java.time.LocalDate

/**
 * Mesai algılama motoru (4857 sayılı İş Kanunu).
 *
 * İki yaklaşımı birleştirir:
 *  1. **Plan bazlı** — gerçek çalışma planlanan vardiyayı aşarsa günlük "mesai" olarak
 *     işaretlenir (anlık algılama / uyarı için, [DayResult.planOvertimeMinutes]).
 *  2. **Haftalık yasal kural** — bordroya esas doğru hesap:
 *     - Sözleşme saatine kadar: normal
 *     - Sözleşme ile 45 saat arası: fazla sürelerle çalışma (×1,25)
 *     - 45 saati aşan: fazla çalışma / mesai (×1,50)
 *
 * Resmî/genel tatil ve hafta tatili günleri ayrı bayraklanır ve haftalık 45 saat
 * hesabına dahil edilmez (ek ücret hakkı bordro motorunda ayrıca işlenir).
 */
class OvertimeDetector {

    fun analyzeWeek(
        weekStart: LocalDate,
        sessions: List<WorkSession>,
        profile: EmployeeProfile,
        plannedShifts: Map<LocalDate, PlannedShift> = emptyMap(),
        holidays: Set<LocalDate> = emptySet()
    ): WeeklyWorkResult {
        val dayResults = sessions.groupBy { it.date }.map { (date, daySessions) ->
            val worked = daySessions.sumOf { it.workedMinutes }
            val night = daySessions.sumOf { it.nightMinutes(profile.nightStart, profile.nightEnd) }
            val isHoliday = date in holidays || date.dayOfWeek == profile.weeklyRestDay
            val plan = plannedShifts[date]
            val planOvertime = if (plan != null) {
                (worked - plan.plannedMinutes).coerceAtLeast(0)
            } else 0L
            DayResult(date, worked, night, isHoliday, planOvertime)
        }.sortedBy { it.date }

        val regularWorked = dayResults.filterNot { it.isHoliday }.sumOf { it.workedMinutes }
        val holidayWorked = dayResults.filter { it.isHoliday }.sumOf { it.workedMinutes }
        val nightTotal = dayResults.sumOf { it.nightMinutes }

        val contractMin = (profile.weeklyContractHours * 60).toLong()
        val fortyFiveMin = 45L * 60

        val normal = regularWorked.coerceAtMost(contractMin)
        val extra = (regularWorked - contractMin).coerceAtLeast(0)
        val fazlaSureBand = (fortyFiveMin - contractMin).coerceAtLeast(0)
        val fazlaSure = extra.coerceAtMost(fazlaSureBand)
        val fazlaCalisma = extra - fazlaSure

        return WeeklyWorkResult(
            weekStart = weekStart,
            totalWorkedMinutes = regularWorked + holidayWorked,
            normalMinutes = normal,
            fazlaSureMinutes = fazlaSure,
            fazlaCalismaMinutes = fazlaCalisma,
            nightMinutes = nightTotal,
            holidayMinutes = holidayWorked,
            days = dayResults
        )
    }

    companion object {
        /** Yıllık fazla çalışma üst sınırı (saat). Aşımda uyarı verilebilir. */
        const val ANNUAL_OVERTIME_LIMIT_HOURS = 270
    }
}
