package com.mesaitakibi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

/** Haftalık vardiya/plan şablonu (haftanın gününe göre). Plan bazlı mesai algılamada kullanılır. */
@Entity(tableName = "shifts")
data class ShiftEntity(
    @PrimaryKey val dayOfWeek: Int, // 1=Pazartesi .. 7=Pazar
    val start: LocalTime,
    val end: LocalTime,
    val breakMinutes: Int = 0,
    val active: Boolean = true
)
