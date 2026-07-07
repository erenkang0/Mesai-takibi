package com.mesaitakibi.data.backup

import com.mesaitakibi.data.local.entity.HolidayEntity
import com.mesaitakibi.data.local.entity.PayrollPeriodEntity
import com.mesaitakibi.data.local.entity.SettingsEntity
import com.mesaitakibi.data.local.entity.ShiftEntity
import com.mesaitakibi.data.local.entity.TaxParametersEntity
import com.mesaitakibi.data.local.entity.TimeEntryEntity
import com.mesaitakibi.data.local.entity.TransactionEntity
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Tüm veritabanının JSON yedeği. java.time alanları ISO-8601 metin olarak saklanır. */
@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: String = "",
    val settings: SettingsDto? = null,
    val timeEntries: List<TimeEntryDto> = emptyList(),
    val shifts: List<ShiftDto> = emptyList(),
    val holidays: List<HolidayDto> = emptyList(),
    val taxParameters: List<TaxParametersDto> = emptyList(),
    val payrollPeriods: List<PayrollPeriodDto> = emptyList(),
    val transactions: List<TransactionDto> = emptyList()
)

@Serializable
data class SettingsDto(
    val name: String, val wageType: String, val grossWage: String,
    val weeklyContractHours: Double, val monthlyHourDivisor: Double,
    val nightStart: String, val nightEnd: String, val weeklyRestDay: Int
)

@Serializable
data class TimeEntryDto(
    val id: Long, val date: String, val clockIn: String, val clockOut: String?,
    val breakMinutes: Int, val note: String?
)

@Serializable
data class ShiftDto(
    val dayOfWeek: Int, val start: String, val end: String,
    val breakMinutes: Int, val active: Boolean
)

@Serializable
data class HolidayDto(val date: String, val name: String, val paid: Boolean)

@Serializable
data class TaxParametersDto(
    val year: Int, val minimumWageGross: String, val sgkCeiling: String,
    val sgkEmployeeRate: String, val unemploymentEmployeeRate: String,
    val stampTaxRate: String, val incomeTaxBracketsJson: String,
    val applyMinimumWageExemption: Boolean
)

@Serializable
data class PayrollPeriodDto(
    val id: Int, val year: Int, val month: Int, val gross: String,
    val fazlaSurePay: String, val fazlaCalismaPay: String, val holidayPay: String,
    val sgkEmployee: String, val unemployment: String, val incomeTax: String,
    val stampTax: String, val totalDeductions: String, val net: String, val generatedAt: String
)

@Serializable
data class TransactionDto(
    val id: Long, val date: String, val amount: String,
    val type: String, val category: String, val note: String?
)

// --- Entity <-> DTO dönüşümleri ---

fun SettingsEntity.toDto() = SettingsDto(
    name, wageType, grossWage, weeklyContractHours, monthlyHourDivisor,
    nightStart.toString(), nightEnd.toString(), weeklyRestDay
)

fun SettingsDto.toEntity() = SettingsEntity(
    id = 0, name = name, wageType = wageType, grossWage = grossWage,
    weeklyContractHours = weeklyContractHours, monthlyHourDivisor = monthlyHourDivisor,
    nightStart = LocalTime.parse(nightStart), nightEnd = LocalTime.parse(nightEnd),
    weeklyRestDay = weeklyRestDay
)

fun TimeEntryEntity.toDto() = TimeEntryDto(
    id, date.toString(), clockIn.toString(), clockOut?.toString(), breakMinutes, note
)

fun TimeEntryDto.toEntity() = TimeEntryEntity(
    id = id, date = LocalDate.parse(date), clockIn = LocalDateTime.parse(clockIn),
    clockOut = clockOut?.let(LocalDateTime::parse), breakMinutes = breakMinutes, note = note
)

fun ShiftEntity.toDto() = ShiftDto(dayOfWeek, start.toString(), end.toString(), breakMinutes, active)

fun ShiftDto.toEntity() = ShiftEntity(
    dayOfWeek = dayOfWeek, start = LocalTime.parse(start), end = LocalTime.parse(end),
    breakMinutes = breakMinutes, active = active
)

fun HolidayEntity.toDto() = HolidayDto(date.toString(), name, paid)

fun HolidayDto.toEntity() = HolidayEntity(date = LocalDate.parse(date), name = name, paid = paid)

fun TaxParametersEntity.toDto() = TaxParametersDto(
    year, minimumWageGross, sgkCeiling, sgkEmployeeRate, unemploymentEmployeeRate,
    stampTaxRate, incomeTaxBracketsJson, applyMinimumWageExemption
)

fun TaxParametersDto.toEntity() = TaxParametersEntity(
    year = year, minimumWageGross = minimumWageGross, sgkCeiling = sgkCeiling,
    sgkEmployeeRate = sgkEmployeeRate, unemploymentEmployeeRate = unemploymentEmployeeRate,
    stampTaxRate = stampTaxRate, incomeTaxBracketsJson = incomeTaxBracketsJson,
    applyMinimumWageExemption = applyMinimumWageExemption
)

fun PayrollPeriodEntity.toDto() = PayrollPeriodDto(
    id, year, month, gross, fazlaSurePay, fazlaCalismaPay, holidayPay,
    sgkEmployee, unemployment, incomeTax, stampTax, totalDeductions, net, generatedAt.toString()
)

fun PayrollPeriodDto.toEntity() = PayrollPeriodEntity(
    id = id, year = year, month = month, gross = gross, fazlaSurePay = fazlaSurePay,
    fazlaCalismaPay = fazlaCalismaPay, holidayPay = holidayPay, sgkEmployee = sgkEmployee,
    unemployment = unemployment, incomeTax = incomeTax, stampTax = stampTax,
    totalDeductions = totalDeductions, net = net, generatedAt = LocalDateTime.parse(generatedAt)
)

fun TransactionEntity.toDto() = TransactionDto(id, date.toString(), amount, type, category, note)

fun TransactionDto.toEntity() = TransactionEntity(
    id = id, date = LocalDate.parse(date), amount = amount, type = type, category = category, note = note
)
