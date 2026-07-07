package com.mesaitakibi.domain.overtime

import com.mesaitakibi.domain.model.EmployeeProfile
import com.mesaitakibi.domain.model.PlannedShift
import com.mesaitakibi.domain.model.WageType
import com.mesaitakibi.domain.model.WorkSession
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OvertimeDetectorTest {

    private val detector = OvertimeDetector()

    private fun profile(contractHours: Double = 45.0) = EmployeeProfile(
        name = "Test",
        wageType = WageType.MONTHLY,
        grossWage = BigDecimal("50000"),
        weeklyContractHours = contractHours,
        weeklyRestDay = DayOfWeek.SUNDAY
    )

    /** Pazartesi başlayan haftada, belirtilen saat kadar tek oturumlu gün üretir. */
    private fun day(monday: LocalDate, offset: Long, hours: Long): WorkSession {
        val date = monday.plusDays(offset)
        return WorkSession(
            date = date,
            clockIn = LocalDateTime.of(date, LocalTime.of(9, 0)),
            clockOut = LocalDateTime.of(date, LocalTime.of(9, 0)).plusHours(hours),
            breakMinutes = 0
        )
    }

    @Test
    fun `40 saatlik sozlesmede 50 saat calisma 5 fazla sure 5 mesai verir`() {
        val monday = LocalDate.of(2026, 1, 5) // Pazartesi
        val sessions = (0..4).map { day(monday, it.toLong(), 10) } // Pzt-Cum, 10'ar saat = 50 saat

        val r = detector.analyzeWeek(monday, sessions, profile(contractHours = 40.0))

        assertEquals(50 * 60L, r.totalWorkedMinutes)
        assertEquals(40 * 60L, r.normalMinutes)
        assertEquals(5 * 60L, r.fazlaSureMinutes)   // 40–45 arası
        assertEquals(5 * 60L, r.fazlaCalismaMinutes) // 45 üstü
        assertTrue(r.hasOvertime)
    }

    @Test
    fun `45 saatlik sozlesmede 48 saat calisma 3 saat mesai fazla sure yok`() {
        val monday = LocalDate.of(2026, 1, 5)
        val sessions = listOf(
            day(monday, 0, 10), day(monday, 1, 10), day(monday, 2, 10),
            day(monday, 3, 10), day(monday, 4, 8)
        ) // 48 saat

        val r = detector.analyzeWeek(monday, sessions, profile(contractHours = 45.0))

        assertEquals(45 * 60L, r.normalMinutes)
        assertEquals(0L, r.fazlaSureMinutes)
        assertEquals(3 * 60L, r.fazlaCalismaMinutes)
    }

    @Test
    fun `45 saat altinda calisma mesai uretmez`() {
        val monday = LocalDate.of(2026, 1, 5)
        val sessions = (0..3).map { day(monday, it.toLong(), 8) } // 32 saat

        val r = detector.analyzeWeek(monday, sessions, profile(contractHours = 45.0))

        assertEquals(32 * 60L, r.normalMinutes)
        assertEquals(0L, r.fazlaSureMinutes)
        assertEquals(0L, r.fazlaCalismaMinutes)
        assertTrue(!r.hasOvertime)
    }

    @Test
    fun `gece vardiyasi gece dakikalarini dogru sayar`() {
        val date = LocalDate.of(2026, 1, 6)
        // 22:00 - 06:00 (ertesi gün) = 8 saat, tamamı gece (20:00-06:00) penceresinde
        val session = WorkSession(
            date = date,
            clockIn = LocalDateTime.of(date, LocalTime.of(22, 0)),
            clockOut = LocalDateTime.of(date.plusDays(1), LocalTime.of(6, 0))
        )
        val r = detector.analyzeWeek(LocalDate.of(2026, 1, 5), listOf(session), profile())
        assertEquals(8 * 60L, r.nightMinutes)
        assertEquals(8 * 60L, session.workedMinutes)
    }

    @Test
    fun `hafta tatili gunu ayri bayraklanir ve 45 saate dahil edilmez`() {
        val monday = LocalDate.of(2026, 1, 5)
        val sunday = monday.plusDays(6) // Pazar = hafta tatili
        val sessions = (0..4).map { day(monday, it.toLong(), 9) } + // 45 saat normal
            day(monday, 6, 5) // Pazar 5 saat tatil çalışması

        val r = detector.analyzeWeek(monday, sessions, profile(contractHours = 45.0))

        assertEquals(5 * 60L, r.holidayMinutes)
        assertEquals(45 * 60L, r.normalMinutes)
        assertEquals(0L, r.fazlaCalismaMinutes) // tatil saatleri 45'e dahil değil
        assertTrue(r.days.any { it.date == sunday && it.isHoliday })
    }

    @Test
    fun `plan bazli mesai plandan fazla calismayi algilar`() {
        val date = LocalDate.of(2026, 1, 6)
        val session = WorkSession(
            date = date,
            clockIn = LocalDateTime.of(date, LocalTime.of(9, 0)),
            clockOut = LocalDateTime.of(date, LocalTime.of(19, 0)) // 10 saat
        )
        val plan = mapOf(date to PlannedShift(LocalTime.of(9, 0), LocalTime.of(17, 0))) // 8 saat

        val r = detector.analyzeWeek(
            LocalDate.of(2026, 1, 5), listOf(session), profile(), plannedShifts = plan
        )

        val dayResult = r.days.first { it.date == date }
        assertEquals(2 * 60L, dayResult.planOvertimeMinutes) // 10 - 8 = 2 saat
    }
}
