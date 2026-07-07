package com.mesaitakibi.domain.model

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Gerçek bir çalışma oturumu (puantaj kaydı): giriş–çıkış ve mola.
 * Gece yarısını geçen vardiyalarda [clockOut] ertesi güne ait olabilir.
 */
data class WorkSession(
    val date: LocalDate,
    val clockIn: LocalDateTime,
    val clockOut: LocalDateTime,
    val breakMinutes: Int = 0
) {
    /** Net çalışılan dakika (mola düşülmüş). */
    val workedMinutes: Long
        get() = (Duration.between(clockIn, clockOut).toMinutes() - breakMinutes).coerceAtLeast(0)

    /**
     * Gece penceresiyle kesişen çalışma dakikası. Pencere gece yarısını aşabilir
     * (ör. 20:00–06:00). Yaklaşık hesap: net çalışma süresini aşamaz.
     */
    fun nightMinutes(nightStart: LocalTime, nightEnd: LocalTime): Long {
        var total = 0L
        var cursor = clockIn
        while (cursor.isBefore(clockOut)) {
            if (isNight(cursor.toLocalTime(), nightStart, nightEnd)) total++
            cursor = cursor.plusMinutes(1)
        }
        return total.coerceAtMost(workedMinutes)
    }

    private fun isNight(t: LocalTime, start: LocalTime, end: LocalTime): Boolean =
        if (start.isBefore(end)) {
            !t.isBefore(start) && t.isBefore(end)
        } else { // pencere gece yarısını aşıyor
            !t.isBefore(start) || t.isBefore(end)
        }
}
