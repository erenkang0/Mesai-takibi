package com.mesaitakibi.domain.model

import java.time.Duration
import java.time.LocalTime

/** Planlanan vardiya. Plan bazlı mesai algılamada gerçek çalışmayla karşılaştırılır. */
data class PlannedShift(
    val start: LocalTime,
    val end: LocalTime,
    val breakMinutes: Int = 0
) {
    /** Planlanan net çalışma dakikası (gece yarısını aşan vardiya desteklenir). */
    val plannedMinutes: Long
        get() {
            val raw = if (end.isAfter(start)) {
                Duration.between(start, end).toMinutes()
            } else {
                Duration.between(start, end).toMinutes() + 24 * 60
            }
            return (raw - breakMinutes).coerceAtLeast(0)
        }
}
