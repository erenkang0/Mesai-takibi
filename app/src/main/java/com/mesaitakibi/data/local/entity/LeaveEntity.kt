package com.mesaitakibi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/** Kullanılan yıllık izin kaydı. */
@Entity(tableName = "leaves")
data class LeaveEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: LocalDate,
    val days: Int,
    val note: String? = null
)
