package com.mesaitakibi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/** Kişisel finans işlemi (gelir/gider). */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val amount: String,
    val type: String, // INCOME / EXPENSE
    val category: String,
    val note: String? = null
)
