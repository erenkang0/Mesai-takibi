package com.mesaitakibi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Puantaj/mesai kaydı. [clockOut] null ise oturum devam ediyor demektir
 * (Dashboard'daki "işe başla / işi bitir" akışı için).
 */
@Entity(tableName = "time_entries")
data class TimeEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val clockIn: LocalDateTime,
    val clockOut: LocalDateTime? = null,
    val breakMinutes: Int = 0,
    val note: String? = null
)
