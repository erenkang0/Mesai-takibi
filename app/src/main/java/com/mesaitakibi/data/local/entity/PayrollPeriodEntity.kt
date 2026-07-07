package com.mesaitakibi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/** Hesaplanmış aylık bordro dönemi (anlık kopya). Para değerleri metin (BigDecimal). */
@Entity(tableName = "payroll_periods")
data class PayrollPeriodEntity(
    @PrimaryKey val id: Int, // year * 100 + month
    val year: Int,
    val month: Int,
    val gross: String,
    val fazlaSurePay: String,
    val fazlaCalismaPay: String,
    val holidayPay: String,
    val sgkEmployee: String,
    val unemployment: String,
    val incomeTax: String,
    val stampTax: String,
    val totalDeductions: String,
    val net: String,
    val generatedAt: LocalDateTime
)
