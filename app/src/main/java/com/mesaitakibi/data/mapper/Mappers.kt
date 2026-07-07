package com.mesaitakibi.data.mapper

import com.mesaitakibi.data.local.entity.SettingsEntity
import com.mesaitakibi.data.local.entity.TaxParametersEntity
import com.mesaitakibi.data.local.entity.TimeEntryEntity
import com.mesaitakibi.data.local.entity.TransactionEntity
import com.mesaitakibi.domain.finance.FinanceTransaction
import com.mesaitakibi.domain.finance.TransactionType
import com.mesaitakibi.domain.model.EmployeeProfile
import com.mesaitakibi.domain.model.WageType
import com.mesaitakibi.domain.model.WorkSession
import com.mesaitakibi.domain.payroll.TaxBracket
import com.mesaitakibi.domain.payroll.TaxParameters
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalTime

/** Gelir vergisi dilimlerinin JSON temsili. */
@Serializable
data class TaxBracketDto(val upTo: String?, val rate: String)

private val json = Json { ignoreUnknownKeys = true }

// --- EmployeeProfile <-> SettingsEntity ---

fun SettingsEntity.toDomain(): EmployeeProfile = EmployeeProfile(
    name = name,
    wageType = WageType.valueOf(wageType),
    grossWage = BigDecimal(grossWage),
    weeklyContractHours = weeklyContractHours,
    monthlyHourDivisor = monthlyHourDivisor,
    nightStart = nightStart,
    nightEnd = nightEnd,
    weeklyRestDay = DayOfWeek.of(weeklyRestDay)
)

fun EmployeeProfile.toEntity(): SettingsEntity = SettingsEntity(
    id = 0,
    name = name,
    wageType = wageType.name,
    grossWage = grossWage.toPlainString(),
    weeklyContractHours = weeklyContractHours,
    monthlyHourDivisor = monthlyHourDivisor,
    nightStart = nightStart,
    nightEnd = nightEnd,
    weeklyRestDay = weeklyRestDay.value
)

// --- TaxParameters <-> TaxParametersEntity ---

fun TaxParametersEntity.toDomain(): TaxParameters = TaxParameters(
    year = year,
    minimumWageGross = BigDecimal(minimumWageGross),
    sgkCeiling = BigDecimal(sgkCeiling),
    sgkEmployeeRate = BigDecimal(sgkEmployeeRate),
    unemploymentEmployeeRate = BigDecimal(unemploymentEmployeeRate),
    stampTaxRate = BigDecimal(stampTaxRate),
    incomeTaxBrackets = json.decodeFromString<List<TaxBracketDto>>(incomeTaxBracketsJson)
        .map { TaxBracket(it.upTo?.let(::BigDecimal), BigDecimal(it.rate)) },
    applyMinimumWageExemption = applyMinimumWageExemption
)

fun TaxParameters.toEntity(): TaxParametersEntity = TaxParametersEntity(
    year = year,
    minimumWageGross = minimumWageGross.toPlainString(),
    sgkCeiling = sgkCeiling.toPlainString(),
    sgkEmployeeRate = sgkEmployeeRate.toPlainString(),
    unemploymentEmployeeRate = unemploymentEmployeeRate.toPlainString(),
    stampTaxRate = stampTaxRate.toPlainString(),
    incomeTaxBracketsJson = json.encodeToString(
        incomeTaxBrackets.map { TaxBracketDto(it.upTo?.toPlainString(), it.rate.toPlainString()) }
    ),
    applyMinimumWageExemption = applyMinimumWageExemption
)

// --- TimeEntryEntity -> WorkSession (yalnızca çıkış yapılmış oturumlar) ---

fun TimeEntryEntity.toWorkSessionOrNull(): WorkSession? {
    val out = clockOut ?: return null
    return WorkSession(date = date, clockIn = clockIn, clockOut = out, breakMinutes = breakMinutes)
}

// --- TransactionEntity <-> FinanceTransaction ---

fun TransactionEntity.toDomain(): FinanceTransaction = FinanceTransaction(
    amount = BigDecimal(amount),
    type = TransactionType.valueOf(type),
    category = category,
    year = date.year,
    month = date.monthValue
)
