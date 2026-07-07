package com.mesaitakibi.domain.overtime

/** Yıllık fazla çalışma sınırına (270 saat) göre durum seviyesi. */
enum class OvertimeLevel { NORMAL, WARNING, CRITICAL, EXCEEDED }

data class AnnualOvertimeStatus(
    val usedHours: Double,
    val limitHours: Int,
    val ratio: Double,
    val level: OvertimeLevel
) {
    val remainingHours: Double get() = (limitHours - usedHours).coerceAtLeast(0.0)
}

/** Yıllık fazla çalışma (mesai) sınırı takibi. */
object AnnualOvertimeTracker {
    const val LIMIT_HOURS = OvertimeDetector.ANNUAL_OVERTIME_LIMIT_HOURS

    fun status(usedHours: Double): AnnualOvertimeStatus {
        val ratio = if (LIMIT_HOURS > 0) usedHours / LIMIT_HOURS else 0.0
        val level = when {
            ratio >= 1.0 -> OvertimeLevel.EXCEEDED
            ratio >= 0.90 -> OvertimeLevel.CRITICAL
            ratio >= 0.75 -> OvertimeLevel.WARNING
            else -> OvertimeLevel.NORMAL
        }
        return AnnualOvertimeStatus(usedHours, LIMIT_HOURS, ratio, level)
    }
}
