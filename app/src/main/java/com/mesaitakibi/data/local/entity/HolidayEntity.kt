package com.mesaitakibi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/** Resmî/genel tatil günü. Tatilde çalışma bordroda ek ücret olarak işlenir. */
@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey val date: LocalDate,
    val name: String,
    val paid: Boolean = true
)
