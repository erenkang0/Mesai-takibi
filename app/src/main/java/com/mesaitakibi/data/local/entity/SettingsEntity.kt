package com.mesaitakibi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

/** Tek satırlık çalışan profili / ayarlar (id daima 0). Para değerleri metin (BigDecimal) olarak. */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0,
    val name: String = "",
    val wageType: String = "MONTHLY",
    val grossWage: String = "0",
    val weeklyContractHours: Double = 45.0,
    val monthlyHourDivisor: Double = 225.0,
    val nightStart: LocalTime = LocalTime.of(20, 0),
    val nightEnd: LocalTime = LocalTime.of(6, 0),
    val weeklyRestDay: Int = 7 // DayOfWeek.SUNDAY.value
)
