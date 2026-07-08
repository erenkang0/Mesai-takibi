package com.mesaitakibi.data.repository

import com.mesaitakibi.data.local.DefaultData
import com.mesaitakibi.data.local.dao.HolidayDao
import com.mesaitakibi.data.local.dao.SettingsDao
import com.mesaitakibi.data.local.dao.ShiftDao
import com.mesaitakibi.data.local.dao.TimeEntryDao
import com.mesaitakibi.data.local.entity.TimeEntryEntity
import com.mesaitakibi.data.mapper.toDomain
import com.mesaitakibi.data.mapper.toWorkSessionOrNull
import com.mesaitakibi.domain.model.PlannedShift
import com.mesaitakibi.domain.overtime.OvertimeDetector
import com.mesaitakibi.domain.overtime.WeeklyWorkResult
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeTrackingRepository @Inject constructor(
    private val timeDao: TimeEntryDao,
    private val shiftDao: ShiftDao,
    private val holidayDao: HolidayDao,
    private val settingsDao: SettingsDao,
    private val detector: OvertimeDetector
) {
    fun observeOpenSession(): Flow<TimeEntryEntity?> = timeDao.observeOpenSession()

    suspend fun openSession(): TimeEntryEntity? = timeDao.getOpenSession()

    /** Verilen günün aktif plan/vardiya bitiş saati (mesai bu saatten sonra başlar). */
    suspend fun plannedEndForDate(date: LocalDate): LocalTime? =
        shiftDao.getActive().firstOrNull { it.dayOfWeek == date.dayOfWeek.value }?.end

    fun observeAll(): Flow<List<TimeEntryEntity>> = timeDao.observeAll()

    fun observeWeek(weekStart: LocalDate): Flow<List<TimeEntryEntity>> =
        timeDao.observeBetween(weekStart, weekStart.plusDays(6))

    /** İşe başla: açık oturum yoksa yeni bir giriş oluşturur. */
    suspend fun clockIn(now: LocalDateTime = LocalDateTime.now()) {
        if (timeDao.getOpenSession() == null) {
            timeDao.insert(TimeEntryEntity(date = now.toLocalDate(), clockIn = now, clockOut = null))
        }
    }

    /** İşi bitir: açık oturumu şimdiki zamanla kapatır. */
    suspend fun clockOut(now: LocalDateTime = LocalDateTime.now()) {
        val open = timeDao.getOpenSession() ?: return
        timeDao.update(open.copy(clockOut = now))
    }

    suspend fun addOrUpdate(entry: TimeEntryEntity) {
        if (entry.id == 0L) timeDao.insert(entry) else timeDao.update(entry)
    }

    suspend fun delete(entry: TimeEntryEntity) = timeDao.delete(entry)

    /** Haftalık çalışma/mesai analizi (mesai algılama motoru). */
    suspend fun analyzeWeek(weekStart: LocalDate): WeeklyWorkResult {
        val weekEnd = weekStart.plusDays(6)
        val sessions = timeDao.getBetween(weekStart, weekEnd).mapNotNull { it.toWorkSessionOrNull() }
        val profile = (settingsDao.get() ?: DefaultData.defaultSettings()).toDomain()
        val holidays = holidayDao.getBetween(weekStart, weekEnd).map { it.date }.toSet()
        val plannedShifts: Map<LocalDate, PlannedShift> = shiftDao.getActive().associate { shift ->
            weekStart.plusDays((shift.dayOfWeek - 1).toLong()) to
                PlannedShift(shift.start, shift.end, shift.breakMinutes)
        }
        return detector.analyzeWeek(weekStart, sessions, profile, plannedShifts, holidays)
    }

    /** Yıl boyunca kümülatif fazla çalışma (mesai) saati — yıllık 270 saat sınırı takibi için. */
    suspend fun annualOvertimeHours(year: Int): Double {
        var minutes = 0L
        var week = LocalDate.of(year, 1, 1).with(DayOfWeek.MONDAY)
        val yearEnd = LocalDate.of(year, 12, 31)
        while (!week.isAfter(yearEnd)) {
            if (week.year == year || week.plusDays(6).year == year) {
                minutes += analyzeWeek(week).fazlaCalismaMinutes
            }
            week = week.plusDays(7)
        }
        return minutes / 60.0
    }

    companion object {
        /** Verilen tarihi içeren ISO haftasının Pazartesi'si. */
        fun weekStartOf(date: LocalDate): LocalDate = date.with(DayOfWeek.MONDAY)
    }
}
